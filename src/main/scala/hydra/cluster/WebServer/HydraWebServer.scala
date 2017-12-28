package hydra.cluster.WebServer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{complete, _}
import akka.stream.ActorMaterializer
import hydra.cluster.Cons.HydraConfig
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import hydra.cluster.Log.HydraLogger
import hydra.cluster.WebServer.models.{AppRequest, AppRequestFormats, AppRequestJsonFormat}
import hydra.cluster.common.DeployService.DeployReq



/**
  * For Hydra
  * Created by whereby[Tao Zhou](187225577@qq.com) on 2017/12/25
  */

object HydraWebServer extends  AppRequestFormats with HydraLogger with AppRequestJsonFormat{

  val config = HydraConfig.load()

  def createWebServer(system2:ActorSystem): Unit ={
    implicit val system =system2
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher


    val deployServiceProxy = system.actorOf(ClusterSingletonProxy.props(
      singletonManagerPath = "/user/deployservice",
      settings = ClusterSingletonProxySettings(system)),
      name = "deployserviceProxyWeb")

    val route =
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
        }
      }~
    post{
      path("app"){
        entity(as[AppRequest]){ app =>
          entity(as[HttpEntity]){appRequest =>
            appRequest.dataBytes.map(_.utf8String).runForeach(data => {
              deployServiceProxy ! DeployReq(data)
            })
            complete("App started")
          }
        }
/* * another way to handle request.
      entity(as[HttpEntity]){appRequest =>
          var registered = false
          appRequest.dataBytes.map(_.utf8String).runForeach(data => {
            val body = Json.parse(data)
            try {
              body.asOpt[AppRequest] match {
                case Some(appRequest) =>
                  registered = true
                  println("app registe success")
              }
            }catch {
              case _ => println("input is not correct")
            }
          })
          complete("App started")
        }*/
      }
    }
    val hostName = config.getString("hydra.web.hostname")
    val port = config.getInt("hydra.web.port")
    val bindFuture = Http().bindAndHandle(route,hostName,port)
    logger.info(s"Server online at http://$hostName:$port/")
  }


}
