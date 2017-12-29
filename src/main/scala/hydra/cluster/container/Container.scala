package hydra.cluster.container

import akka.actor.{Actor, ActorLogging, Cancellable, PoisonPill}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import hydra.cluster.Cons.{AppRequst, HydraTopic}
import hydra.cluster.container.Container._
import hydra.cluster.deploy.DeployService.UnDeployMsg
import play.api.libs.json.Json
import hydra.cluster.common.DeployService.DeployReq

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.sys.process._
import scalaj.http._
import scala.language.postfixOps

/**
  * Created by TaoZhou(whereby@live.cn) on 08/10/2017.
  */
class Container extends Actor with ActorLogging {

  import scala.concurrent.ExecutionContext.Implicits.global

  val mediator = DistributedPubSub(context.system).mediator
  var appname = ""
  var appConfig = ""
  var preStartCmd: List[String] = List()
  var startCmd: List[String] = List()
  val healthRecordLength = 10
  var healthRecord = Array.fill(healthRecordLength)(0)
  var healthIndex = 0
  lazy val containerAddress = Cluster(context.system).selfAddress
  lazy val osString = System.getProperty("os.name")
  var healthCheckEndpoint = "http://localhost:9000/hello"

  var cancellable: Option[Cancellable] = None


  def startHealthCheck(): Unit = {
    cancellable = Some(context.system.scheduler.schedule(0 seconds, 5 second, self, TickMsg))
  }

  def doHealthCheck(): Boolean = {
    var response: HttpResponse[String] = null
    try {
      response = Http(healthCheckEndpoint).asString
    }
    catch {
      case _: Throwable => response = null
    }
    log.info(s"Result of health : $response")
    var result = 0
    if (response == null) {
      result = 1
    }
    healthRecord(healthIndex) = result
    healthIndex = (healthIndex + 1) % healthRecordLength
    if (healthRecord.sum > healthRecordLength / 2) {
      for (i <- 0 until healthRecordLength) {
        healthRecord(i) = 0
      }
      return true
    }
    return false
  }

  def parseConfigure(configureString: String) = {
    appConfig = configureString
    val configJson = Json.parse(configureString)
    appname = (configJson \ AppRequst.appname).asOpt[String].getOrElse("")
    (configJson \ AppRequst.prestartcmd).asOpt[List[String]] map {
      seqcmd => preStartCmd = seqcmd
    }
    (configJson \ AppRequst.startcmd).asOpt[List[String]] map {
      startcmd =>
        if (osString.toLowerCase().startsWith("win")) {
          startCmd = "cmd.exe" :: "/c" :: startcmd
        } else {
          startCmd = "bash" :: "-c" :: startcmd
        }
    }
    (configJson \AppRequst.healthcheck).asOpt[String]map{
      healthCheckStr=> healthCheckEndpoint = healthCheckStr
    }
  }


  def receive = {
    case TickMsg =>
      log.info(s"$appname checked")
      val result = doHealthCheck()
      if (result) {
        self ! RelocateMsg
        cancellable.map {
          cancellable => cancellable.cancel()
        }
        log.info(s"$appname is to relocate due to health check failed")
      }

    case RelocateMsg =>
      log.info(s"$appname is about to reloacte")
      mediator ! Publish(HydraTopic.deployReq, DeployReq(appConfig))
      self ! FinishMsg

    case InitialMsg(appConfig) => parseConfigure(appConfig)
      self ! StartMsg

    case StartMsg =>
      //runner ! StartCmd(startCmd)
      log.info(s"Start App container With: $startCmd")
      Future(startCmd.!)
      log.info(s"$appname has deploy on $containerAddress")
      startHealthCheck()

    case FinishMsg =>
      mediator ! Publish(HydraTopic.deployedMsg, UnDeployMsg(containerAddress, appConfig))
      log.info(s"$appname is undeployed")
      //context stop self #https://stackoverflow.com/questions/18971088/dead-letters-encountered-as-soon-as-actors-are-placed-into-router
      context.system.scheduler.scheduleOnce(1 second) {
        self ! PoisonPill
      }
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    preStartCmd.!
    cancellable = None
  }

}

object Container {

  case class RelocateMsg()

  case class TickMsg()

  case class StartMsg()

  case class FinishMsg()

  case class InitialMsg(appConfig: String)

  case class StartCmd(startcmds: List[String])

}
