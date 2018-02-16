package hydra.cluster.WebServer

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.Cluster
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{complete, _}
import akka.stream.ActorMaterializer
import hydra.cluster.Cons.HydraConfig
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import akka.util.Timeout
import hydra.cluster.Log.HydraLogger
import hydra.cluster.WebServer.models.{AppRequest, AppRequestFormats, AppRequestJsonFormat}
import hydra.cluster.common.DeployService.DeployReq
import hydra.cluster.data.{AppListTrait, ApplicationListManager}
import hydra.cluster.external.models.LoaderMSG.{ExternalLoaderRequest, QueryExternalClass}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.pattern._
import hydra.cluster.external.models.ExternalLoaderRequestJsonFormat


/**
  * For Hydra
  * Created by whereby[Tao Zhou](187225577@qq.com) on 2017/12/25
  */

object HydraWebServer extends  AppRequestFormats with HydraLogger with AppRequestJsonFormat with ExternalLoaderRequestJsonFormat{

  val config = HydraConfig.load()

  var applicationList: AppListTrait = _
  implicit val timeout = Timeout(Duration(5, TimeUnit.SECONDS))

  def createWebServer(system2:ActorSystem): Unit ={
    implicit val system =system2
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher
    val selfAddress = Cluster(system2).selfAddress
    applicationList  = ApplicationListManager.getApplicationList(selfAddress)
    val deployServiceProxy = system.actorOf(ClusterSingletonProxy.props(
      singletonManagerPath = "/user/deployservice",
      settings = ClusterSingletonProxySettings(system)),
      name = "deployserviceProxyWeb")
    val  externalLoader:ActorRef =Await.result( system.actorSelection("/user/externalLoader").resolveOne(), timeout.duration)
    val route =
      get{
        path("external"){
        //Maybe have more elegent way to handle future
        val result =(externalLoader ? QueryExternalClass) map{
          case msg:String=>msg
          case _=>""
        }
        val resultString = Await.result(result,timeout.duration)
       // complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,resultString))
      }}~
        path("hello") {
          get {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
          }
        }~
      post{
        path("external"){
        entity(as[ExternalLoaderRequest]){externalReq=>
          system.actorSelection("/user/externalLoader").resolveOne().map{
            externalLoader=>externalLoader ! ExternalLoaderRequest(externalReq.jarAddress, externalReq.className)
          }
          complete("Actor created")
        }
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
      }
    }~
        get{
          path("app" / Segment){ appname=>
            val hostIPs = applicationList.appList.get(appname)match {
              case Some(hostSeq) => hostSeq.map(address => address.host.get)
              case _=> List()
            }
            complete(hostIPs)
          }
        }
    val hostName = config.getString("akka.remote.netty.tcp.hostname")
    val port = config.getInt("hydra.web.port")
    Http().bindAndHandle(route,hostName,port)
    logger.info(s"Server online at http://$hostName:$port/")
  }


}
