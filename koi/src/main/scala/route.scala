import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest

import scala.collection.mutable

//trait Process {
//  def process(ctx: ChannelHandlerContext, req: FullHttpRequest): Unit
//}

type Process = (ctx: ChannelHandlerContext, req: FullHttpRequest) => Unit

class Router {
  private val routeTable = new mutable.HashMap[String, Process]()

  def addRoute(path: String, process: Process): Unit = routeTable.addOne(path, process)

  def dispatch(ctx: ChannelHandlerContext, req: FullHttpRequest): Unit = {
    val path = req.uri()
    if routeTable.contains(path) then
      routeTable(path)(ctx, req)
  }
}