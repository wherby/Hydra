package hydra.cluster.external

import java.io.File

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, Address, Deploy, Props}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import akka.event.LoggingAdapter
import akka.remote.RemoteScope
import hydra.cluster.constent.{DispatcherName, HydraConfig, HydraTopic}
import hydra.cluster.external.models.LoaderMSG._
import play.api.libs.json.Json

import scala.util.Random

/**
  * For hydra.cluster.ExternalLoader in Hydra
  * Created by whereby[Tao Zhou](187225577@qq.com) on 2018/2/16
  */
object ExternalActorLoader {
  lazy val config = HydraConfig.load()

  def loadClassFromJar(externalLoaderRequest: ExternalLoaderRequest, context: ActorContext, log: LoggingAdapter): Option[ActorRef] = {
    val selfAddress = Cluster(context.system).selfAddress
    val jarAddress = externalLoaderRequest.jarAddress
    val className = externalLoaderRequest.className
    val deployAddress = externalLoaderRequest.address match {
      case Some(address) => val protocol = "akka.tcp"
        val systemname = config.getString("hydra.clustername")
        val addressSplit = address.split(":")
        val addressName = addressSplit(0)
        val port = if (addressSplit.length == 2) {
          addressSplit(1).toInt
        } else {
          2551
        }
        Address.apply(protocol, systemname, Some(addressName), Some(port))
      case _ => selfAddress
    }
    //log.info("Create Address: " + deployAddress.toString)
    try {
      val classLoader = if (this.getClass.getClasses.contains(className)) {
        log.info(s"$className is aready loaded.")
        this.getClass.getClassLoader
      } else {
        new java.net.URLClassLoader(
          Array(new File(jarAddress).toURI.toURL),
          /*
         * need to specify parent, so we have all class instances
         * in current context
         */
          this.getClass.getClassLoader)
      }
      log.info(s"loading $className from $jarAddress")
      val allclazz = classLoader.getClass.getClasses.map(a => a.toString).mkString(";")
      log.info(s"All clazz: $allclazz")
      val clazz = Class.forName(className, true, classLoader)
      log.info("Get : " + clazz.getCanonicalName)
      val actorMame = externalLoaderRequest.actorName match {
        case Some(actorNameStr) => actorNameStr
        case None=> clazz.getSimpleName + Random.nextInt()
      }
      val actorRef = context.actorOf(Props(clazz)
        .withDispatcher(DispatcherName.externalDispatcher)
        .withDeploy(Deploy(scope = RemoteScope(deployAddress))),
        actorMame)
      log.info(s"Create Actor:  " + actorRef.path.toString)
      Some(actorRef)
    } catch {
      case ex: Throwable =>
        log.error(s"Loading $jarAddress failed.due to :" + ex.getMessage)
        None
    }
  }

}

class ExternalActorLoader extends Actor with ActorLogging {

  import scala.collection.mutable.Map

  val externalActorList: Map[String, ActorRef] = Map()
  lazy val config = HydraConfig.load()
  val selfAddress = Cluster(context.system).selfAddress
  val mediator = DistributedPubSub(context.system).mediator

  override def receive: Receive = {
    case msg: ExternalLoaderRequest =>
      val actorRef = ExternalActorLoader.loadClassFromJar(msg, context, log)
      actorRef.map(actorRef => {
        val path = actorRef.path.toString
        externalActorList(path) = actorRef
        mediator ! Publish(HydraTopic.deployExternalActor,
          DeployExternalActor(selfAddress,msg.jarAddress,msg.className,path))
      })
    case QueryExternalClass => log.info(externalActorList.toString())
      sender() ! externalActorList.toString()
    case QueryChildren => val childerenList = context.children.map {
      child => child.path.toString
    }
      log.info(s"Children under $selfAddress are : $childerenList ")
      sender() ! Json.obj("childeren" -> childerenList).toString()
    case DeleteChildren(actorName, address) => externalActorList.get(actorName) map {
      actorRef =>
        context.stop(actorRef)
        log.info("Stop the Actor : " + actorRef.path.toString)
        externalActorList.remove(actorName)
        mediator ! Publish(HydraTopic.deployExternalActor, RemoveExternalActor(selfAddress,actorName))
    }
  }

}
