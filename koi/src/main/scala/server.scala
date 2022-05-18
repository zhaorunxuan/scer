import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.{Channel, ChannelHandlerContext, ChannelInboundHandlerAdapter, ChannelInitializer, EventLoopGroup, SimpleChannelInboundHandler}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.{NioServerSocketChannel, NioSocketChannel}
import io.netty.handler.codec.http.{DefaultFullHttpRequest, DefaultFullHttpResponse, DefaultHttpResponse, FullHttpRequest, HttpHeaderNames, HttpObject, HttpObjectAggregator, HttpRequest, HttpResponseStatus, HttpServerCodec, HttpVersion}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.handler.timeout.ReadTimeoutHandler
import org.slf4j.LoggerFactory

import java.io.{File, RandomAccessFile}

class KoiServer {
  private val log = LoggerFactory.getLogger(classOf[KoiServer])
  private var bootstrap: ServerBootstrap = _
  private var saveChannel: Channel = _
  private var group: NioEventLoopGroup = _
  private val router = Router()

  def listen(port: Int): Unit = {
    group = NioEventLoopGroup()
    bootstrap = ServerBootstrap()
    bootstrap.group(group)
      .channel(classOf[NioServerSocketChannel])
      .childHandler(new ChannelInitializer[NioSocketChannel] {
        override def initChannel(ch: NioSocketChannel): Unit = {
          ch.pipeline().addLast(HttpServerCodec())
            .addLast(HttpObjectAggregator(1 << 30))
            .addLast(ChunkedWriteHandler())
            .addLast(MyChannelHandler(router))
            .addLast(DownloadHandler())
        }
      })

    saveChannel = bootstrap.bind(port).sync().channel()
    log.info(s"koi is running at $port")
  }

  private def close(): Unit = {
    saveChannel.close()
    group.shutdownGracefully()
  }

  def use(path: String, process: Process): Unit = {
    router.addRoute(path, process)
  }
}

object KoiServer {
  def shutdown(hs: KoiServer): Unit = {
    Runtime.getRuntime.addShutdownHook(Thread(() => {
      hs.log.info("koi is stopped")
      hs.close()
    }))
  }
}

class MyChannelHandler(val router: Router) extends ChannelInboundHandlerAdapter {
  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    if msg.isInstanceOf[FullHttpRequest] then
       val req = msg.asInstanceOf[FullHttpRequest]
       if req.uri().startsWith("/download") then
         ctx.fireChannelRead(msg)
       router.dispatch(ctx, req)
  }
}

class DownloadHandler extends ChannelInboundHandlerAdapter {
  private val log = LoggerFactory.getLogger(classOf[DownloadHandler])
  val filePath = "D:\\zhaorx\\Desktop\\demo.txt"

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    val file = File(filePath)
    val raf = new RandomAccessFile(file, "r")
    val fileLength = raf.length
    val res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
    res.headers.set(HttpHeaderNames.CONTENT_LENGTH, fileLength)
    res.headers.set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream")
    res.headers.add(HttpHeaderNames.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", file.getName))
    ctx.write(res)

    import io.netty.channel.ChannelFuture
    import io.netty.channel.ChannelProgressiveFuture
    import io.netty.channel.ChannelProgressiveFutureListener
    import io.netty.channel.DefaultFileRegion
    import io.netty.handler.codec.http.LastHttpContent

    val sendFileFuture = ctx.write(new DefaultFileRegion(raf.getChannel, 0, fileLength), ctx.newProgressivePromise)
    sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
      @throws[Exception]
      override def operationComplete(future: ChannelProgressiveFuture): Unit = {
        log.info("file {} transfer complete.", file.getName)
        raf.close()
      }

      @throws[Exception]
      override def operationProgressed(future: ChannelProgressiveFuture, progress: Long, total: Long): Unit = {
        if total < 0 then log.warn("file {} transfer progress: {}", file.getName, progress)
        else log.debug("file {} transfer progress: {}/{}", file.getName, progress, total)
      }
    })
    ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
    ctx.close()
  }
}