package hydra.cluster.deploy

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Address, Deploy, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.remote.RemoteScope
import hydra.cluster.eventlistener.Aggregator.FailedMsg
import hydra.cluster.container.Container.InitialMsg
import hydra.cluster.deploy.DeployService.{DeployRecipe, DeployedMsg, UnDeployMsg}
import play.api.libs.json.Json
import hydra.cluster.common.DeployService.DeployReq

import scala.util.Random
import akka.cluster.Cluster
import hydra.cluster.constent.{AppRequst, HydraConfig, HydraTopic}
import hydra.cluster.logger.HydraLogger
import hydra.cluster.data.ApplicationListManager

/**
  * Created by TaoZhou(whereby@live.cn) on 26/09/2017.
  */

class DeployService extends Actor with ActorLogging {

  import akka.cluster.pubsub.DistributedPubSubMediator.Publish

  val mediator = DistributedPubSub(context.system).mediator
  val config = HydraConfig.load()
  lazy val containerClazz: String = config.getString("hydra.container")
  val deployScheduler = context.actorOf(Props[DeployScheduler], "deployScheduler")
  val selfAddress = Cluster(context.system).selfAddress

  override def preStart(): Unit = {
    mediator ! Subscribe(HydraTopic.deployReq, self)
    mediator ! Subscribe(HydraTopic.nodeFail, self)
  }

  def receive = {
    case DeployReq(appconfig) =>
      deployScheduler ! DeployReq(appconfig)
    case DeployRecipe(appconfig, sysAddress, containerClass) =>
      val configJson = Json.parse(appconfig)
      val appName = (configJson \ AppRequst.appname).asOpt[String].getOrElse("app" + Random.nextString(3))
      val container = DeployService.tryToInstanceDeployActor(containerClass.getOrElse(containerClazz), sysAddress, context.system, appName + "Container" + Random.nextInt(1000).toString)
      container.map {
        container =>
          container ! InitialMsg(appconfig)
          mediator ! Publish(HydraTopic.deployedMsg, DeployedMsg(sysAddress, appconfig))
          log.info("Published Deploy message for :" + appconfig)
      }
    case UnDeployMsg(system, app) =>
      mediator ! Publish(HydraTopic.deployedMsg, UnDeployMsg(system, app))
    case FailedMsg(address, time) =>
      redeployApplication(address)
    case _ =>
  }

  private def redeployApplication(address: Address) = {
    log.info(s"Deploy serice handle fialed nod: $address")
    val systemlist = ApplicationListManager.getApplicationList(selfAddress).systemlist
    val appconfigList: List[String] = systemlist.get(address).getOrElse(List())
    log.info(s"applist: $appconfigList")
    log.info(s"systemList: $systemlist")
    ApplicationListManager.applicationListMap.remove(address)
    ApplicationListManager.applicationListMap.values.map {
      applist => applist.removeSystem(address)
    }
    appconfigList map {
      appconfig =>
        log.info(s"For Node Failed the app will redeploy: $appconfig")
        self ! DeployReq(appconfig)
    }
  }
}

object DeployService extends HydraLogger{

  // final case class DeployReq(appconfigString: String)

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
      case ex: Throwable => logger.error("Actor Failed " + ex.getMessage() + " " + ex.getStackTrace)
        None
    }
  }

}


