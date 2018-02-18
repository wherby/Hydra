package hydra.cluster.web

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.Cluster
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{as, complete, entity, get, path, post}
import hydra.cluster.external.models.LoaderMSG.{DeleteChildren, ExternalLoaderRequest, QueryChildren, QueryExternalClass}
import akka.pattern._

import scala.concurrent.Await
import akka.http.scaladsl.server.Directives.{complete, _}
import akka.util.Timeout
import hydra.cluster.constent.HydraConfig
import hydra.cluster.external.models.{DeleteChildrenJsonFormat, ExternalLoaderRequestJsonFormat, QueryChildrenJsonFormat}

import scala.concurrent.duration.Duration

/**
  * For hydra.cluster.WebServer in Hydra
  * Created by whereby[Tao Zhou](187225577@qq.com) on 2018/2/18
  */
object ExternalRouter extends ExternalLoaderRequestJsonFormat with QueryChildrenJsonFormat with DeleteChildrenJsonFormat {
  def route(system: ActorSystem) = {
    implicit val timeout = Timeout(Duration(5, TimeUnit.SECONDS))
    implicit val executionContext = system.dispatcher
    val externalLoader: ActorRef = Await.result(system.actorSelection("/user/externalLoader").resolveOne(), timeout.duration)
    val config = HydraConfig.load()
    val systemname = config.getString("hydra.clustername")
    path("external") {
      get {
        //Maybe have more elegent way to handle future
        val result = (externalLoader ? QueryExternalClass) map {
          case msg: String => msg
          case _ => ""
        }
        val resultString = Await.result(result, timeout.duration)
        // complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, resultString))
      } ~
      post {
        entity(as[ExternalLoaderRequest]) { externalReq =>
          val externalLoader: String = externalReq.address match {
            case Some(address) => s"akka.tcp://$systemname@$address/user/externalLoader"
            case _ => "/user/externalLoader"
          }
          Cluster(system).system.actorSelection(externalLoader).resolveOne().map {
            externalLoader => externalLoader ! externalReq
          }
          complete("Actor created")
        }
      }
    } ~
    path("queryext") {
      post {
        entity(as[QueryChildren]) { externalQuery =>
          val externalLoader: String = externalQuery.address match {
            case Some(address) => s"akka.tcp://$systemname@$address/user/externalLoader"
            case _ => "/user/externalLoader"
          }
          val result = Cluster(system).system.actorSelection(externalLoader).resolveOne().flatMap {
            externalLoader =>
              (externalLoader ? QueryChildren) map {
                case msg: String => msg
                case _ => ""
              }
          }
          val resultString = Await.result(result, timeout.duration)
          complete(HttpEntity(ContentTypes.`application/json`, resultString))
        }
      } ~
      delete {
        entity(as[DeleteChildren]) { deleteChildren =>
          val externalLoaderStr: String = deleteChildren.address match {
            case Some(address) => s"akka.tcp://$systemname@$address/user/externalLoader"
            case _ => "/user/externalLoader"
          }
          Cluster(system).system.actorSelection(externalLoaderStr).resolveOne().map {
            externalLoader => externalLoader ! deleteChildren
          }
          complete("Actor deleteed")
        }
      }
    }
  }
}
