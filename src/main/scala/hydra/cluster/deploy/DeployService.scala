package hydra.cluster.deploy

import java.util.concurrent.TimeUnit

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
import akka.util.Timeout
import hydra.cluster.constent.{AppRequst, HydraConfig, HydraTopic}
import hydra.cluster.logger.HydraLogger
import hydra.cluster.data.{ApplicationListManager, ExternalActorListTrait}
import hydra.cluster.external.models.LoaderMSG.{ExternalLoaderRequest, RemoveExternalActor}

import scala.concurrent.duration.Duration

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
      redeployExternalActor(address)
    case _ =>
  }

  private def redeployExternalActor(address: Address): Unit ={
    implicit val executionContext = context.dispatcher
    log.info(s"Deploy service handle failed node: $address  for External actor")
    val externalActorList: ExternalActorListTrait = ApplicationListManager.getExternalList(selfAddress)
    val externalActorToBeRedeploy = externalActorList.getApplicationList(address).map {
      actorStr =>
        val jarAddress = (Json.parse(actorStr) \ "jarAddress").asOpt[String]
        val className = (Json.parse(actorStr) \ "className").asOpt[String]
        (Json.parse(actorStr) \ "actorName").asOpt[String].map{
          actorName=> mediator ! Publish(HydraTopic.deployExternalActor, RemoveExternalActor(address,actorName))
        }
        (jarAddress, className) match {
          case (Some(jarAddress), Some(className)) => Some(ExternalLoaderRequest(jarAddress, className, None))
          case _ => None
        }
    }.flatten
    log.info(s"Actor to be deploy on $address is $externalActorToBeRedeploy")
    val allAvailbleSystem = ApplicationListManager.getApplicationList(selfAddress).systemlist.keys.toList.filter(add => add !=address)
    externalActorList.removeSystem(address)
    //Use random deploy strategy for redeploy
    log.info(s"Available systems : $allAvailbleSystem")
    val count = allAvailbleSystem.length
    var cnt = Random.nextInt(1999) 
    implicit val timeout = Timeout(Duration(5, TimeUnit.SECONDS))
    if(count >0){
      for( atorLoaderRequest <- externalActorToBeRedeploy){
        val addressTemp = allAvailbleSystem(cnt % count)
        cnt = cnt +1
        val address = addressTemp.host.getOrElse("localhost") + ":" + addressTemp.port.getOrElse(2551).toString
        val systemname = config.getString("hydra.clustername")
        val externalLoader: String = s"akka.tcp://$systemname@$address/user/externalLoader"
        context.actorSelection(externalLoader).resolveOne().map {
          externalLoader => externalLoader ! atorLoaderRequest
        }
      }
    }
  }

  private def redeployApplication(address: Address) = {
    log.info(s"Deploy serice handle fialed node: $address")
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


