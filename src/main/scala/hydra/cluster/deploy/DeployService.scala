package hydra.cluster.deploy

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Address, Deploy, PoisonPill, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.remote.RemoteScope
import com.typesafe.config.ConfigFactory
import hydra.cluster.container.Container.InitialMsg
import hydra.cluster.deploy.DeployService.{DeployRecipe, DeployReq, DeployedMsg, UnDeployMsg}
import hydra.cluster.ClusterListener.SimpleClusterApp
import play.api.libs.json.Json

import scala.util.Random

/**
  * Created by TaoZhou(whereby@live.cn) on 26/09/2017.
  */

class DeployService extends Actor with ActorLogging {

  import akka.cluster.pubsub.DistributedPubSubMediator.Publish

  val mediator = DistributedPubSub(context.system).mediator
  mediator ! Subscribe("deployReq", self)

  val config = ConfigFactory.load()
  lazy val containerClazz: String = config.getString("hydra.container")
  val deployScheduler = context.actorOf(Props[DeployScheduler], "deployScheduler")

  def receive = {
    case DeployReq(appconfig, containerClass) =>
      deployScheduler ! DeployReq(appconfig, containerClass)
    case DeployRecipe(appconfig, sysAddress, containerClass) =>
      val configJson = Json.parse(appconfig)
      val appName = (configJson \ "appname").asOpt[String].getOrElse("app" + Random.nextString(3))
      val container = DeployService.tryToInstanceDeployActor(containerClass.getOrElse(containerClazz), sysAddress, context.system, appName + "Container"  + Random.nextInt(1000).toString)
      container.map {
        container => container ! InitialMsg(appconfig)
          mediator ! Publish("deploy", DeployedMsg(sysAddress, appconfig))
          log.info("Published Deploy message for :" + appconfig)
      }
    case UnDeployMsg(system, app) =>
      mediator ! Publish("deploy", UnDeployMsg(system, app))
    case _ =>
  }
}

object DeployService {

  final case class DeployReq(appconfigString: String, containerClazz: Option[String] = None)

  final case class DeployRecipe(appconfigString: String, sysAddress: Address, containerClass: Option[String] = None)

  final case class DeployedMsg(address: Address, app: String)

  final case class UnDeployReq(appName: String, bashString: String)

  final case class UnDeployMsg(system: Address, app: String)

  def tryToInstanceDeployActor(className: String, address: Address, system: ActorSystem, actorName: String): Option[ActorRef] = {
    try {
      val clazz = Class.forName(className)
      val actorRef = system.actorOf(Props(clazz).withDeploy(Deploy(scope = RemoteScope(address))), actorName)
      Some(actorRef)
    } catch {
      case ex => println("Actor Failed " + ex.getMessage() + " " + ex.getStackTrace)
        None
    }
  }

  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      val systems = SimpleClusterApp.startup(Seq("2551", "2552", "0"))
      val address = Address("akka.tcp", "ClusterSystem", "127.0.0.1", 2551)
      val actorRef = DeployService.tryToInstanceDeployActor("hydra.cluster.deploy.DeployService", address, systems(0), "aa")
      actorRef.map {
        actorref => actorref ! DeployedMsg(address, "CCCCCC")
      }
    }
    else
      SimpleClusterApp.startup(args)
  }
}


