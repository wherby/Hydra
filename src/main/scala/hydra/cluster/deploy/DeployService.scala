package hydra.cluster.deploy

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Address, Deploy, PoisonPill, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.remote.RemoteScope
import com.typesafe.config.ConfigFactory
import hydra.cluster.deploy.Container.InitialMsg
import hydra.cluster.deploy.DeployService.{DeployedMsg, DeployRecipe, UnDeployMsg}
import hydra.cluster.simple.SimpleClusterApp

/**
  * Created by TaoZhou(whereby@live.cn) on 26/09/2017.
  */

class DeployService extends Actor with ActorLogging {

  import akka.cluster.pubsub.DistributedPubSubMediator.Publish

  val mediator = DistributedPubSub(context.system).mediator

  val config = ConfigFactory.load()
  lazy val containerClazz: String = config.getString("hydra.container")

  def receive = {
    case DeployRecipe(appName, bashString, sysAddress, containerClass) =>
      val container = DeployService.tryToInstanceDeployActor(containerClass.getOrElse(containerClazz), sysAddress, context.system, appName + "Container")
      container.map {
        container => container ! InitialMsg(bashString)
          self ! DeployedMsg(sysAddress, appName)
      }
    case DeployedMsg(system, app) =>
      mediator ! Publish("deploy", DeployedMsg(system, app))
      log.info("Published Deploy message for :" + app)
    case UnDeployMsg(system, app) =>
      mediator ! Publish("deploy", UnDeployMsg(system, app))
    case _ =>
  }
}

object DeployService {

  final case class DeployRecipe(appName: String, bashString: String, sysAddress: Address, containerClass: Option[String] = None)

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


