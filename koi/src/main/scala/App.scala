import io.netty.handler.codec.http.{DefaultFullHttpResponse, HttpHeaderNames, HttpResponseStatus, HttpVersion}

object App {
  def main(args: Array[String]): Unit = {
    val server = KoiServer()
    server.use("/", (ctx, req) => {
      println(req.uri())
      val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
      res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain")
      ctx.writeAndFlush(res)
    })
    server.listen(8880)
    KoiServer.shutdown(server)
  }
}