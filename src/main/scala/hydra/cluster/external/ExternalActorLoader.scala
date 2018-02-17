package hydra.cluster.external

import java.io.File

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, Address, Deploy, Props}
import akka.cluster.Cluster
import akka.event.LoggingAdapter
import akka.remote.RemoteScope
import hydra.cluster.Cons.HydraConfig
import hydra.cluster.external.models.LoaderMSG.{ExternalLoaderRequest, QueryExternalClass}

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
      var classLoader = if (this.getClass.getClasses.contains(className)) {
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
      val actorRef = context.actorOf(Props(clazz)
        .withDispatcher("external-dispatcher")
        .withDeploy(Deploy(scope = RemoteScope(deployAddress))),
        clazz.getSimpleName + Random.nextInt())
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

  override def receive: Receive = {
    case msg: ExternalLoaderRequest =>
      val actorRef = ExternalActorLoader.loadClassFromJar(msg, context, log)
      actorRef.map(actorRef => {
        val path = actorRef.path.toString
        externalActorList(path) = actorRef
      })
    case QueryExternalClass => log.info(externalActorList.toString())
      sender() ! externalActorList.toString()
  }

}
