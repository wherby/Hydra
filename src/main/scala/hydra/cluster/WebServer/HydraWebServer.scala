package hydra.cluster.WebServer



import akka.actor.{ ActorSystem}

import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import hydra.cluster.Cons.HydraConfig

import hydra.cluster.Log.HydraLogger
import akka.http.scaladsl.server.Directives.{ _}

/**
  * For Hydra
  * Created by whereby[Tao Zhou](187225577@qq.com) on 2017/12/25
  */

object HydraWebServer extends HydraLogger {

  val config = HydraConfig.load()


  def createWebServer(system2: ActorSystem): Unit = {
    implicit val system = system2
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val webRouter = WebRouter.webRouter(system)
    val externalRouter = ExternalRouter.route(system)
    val route = webRouter ~ externalRouter

    val hostName = config.getString("akka.remote.netty.tcp.hostname")
    val port = config.getInt("hydra.web.port")
    Http().bindAndHandle(route, hostName, port)
    logger.info(s"Server online at http://$hostName:$port/")
  }


}
