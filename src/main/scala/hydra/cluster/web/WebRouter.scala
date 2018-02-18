package hydra.cluster.web

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import akka.http.scaladsl.server.Directives.{complete, _}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{as, complete, entity, get, path, post}
import akka.stream.ActorMaterializer
import hydra.cluster.web.models.{AppRequest, AppRequestJsonFormat}
import hydra.cluster.common.DeployService.DeployReq
import hydra.cluster.data.ApplicationListManager

/**
  * For hydra.cluster.WebServer in Hydra
  * Created by whereby[Tao Zhou](187225577@qq.com) on 2018/2/18
  */
object WebRouter extends AppRequestJsonFormat{
  def webRouter(system2:ActorSystem)={
    implicit val system = system2
    implicit val materializer = ActorMaterializer()

    val selfAddress = Cluster(system2).selfAddress
    val applicationList = ApplicationListManager.getApplicationList(selfAddress)

    val deployServiceProxy = system.actorOf(ClusterSingletonProxy.props(
      singletonManagerPath = "/user/deployservice",
      settings = ClusterSingletonProxySettings(system)),
      name = "deployserviceProxyWeb")
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    } ~
    post {
      path("app") {
        entity(as[AppRequest]) { app =>
          entity(as[HttpEntity]) { appRequest =>
            appRequest.dataBytes.map(_.utf8String).runForeach(data => {
              deployServiceProxy ! DeployReq(data)
            })
            complete("App started")
          }
        }
      }
    } ~
    get {
      path("app" / Segment) { appname =>
        val hostIPs = applicationList.appList.get(appname) match {
          case Some(hostSeq) => hostSeq.map(address => address.host.get)
          case _ => List()
        }
        complete(hostIPs)
      }
    }
  }
}
