package hydra.cluster.external

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import hydra.cluster.external.models.LoaderMSG.{ExternalLoaderRequest, QueryExternalClass}

import scala.util.Random

/**
  * For hydra.cluster.ExternalLoader in Hydra
  * Created by whereby[Tao Zhou](187225577@qq.com) on 2018/2/16
  */
class ExternalActorLoader extends Actor with ActorLogging {
  import scala.collection.mutable.Map
  val externalActorList: Map[String,ActorRef] = Map()

  def loadClassFromJar(jarAddress: String, className: String):Option[ActorRef] = {
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
      val actorRef = context.actorOf(Props(clazz), clazz.getSimpleName + Random.nextInt())
      log.info(s"Create Actor:  " + actorRef.path.toString)
      Some(actorRef)
    } catch {
      case ex: Throwable =>
        log.error(s"Loading $jarAddress failed.due to :" + ex.getMessage)
        None
    }
  }

  override def receive: Receive = {
    case ExternalLoaderRequest(jarAddress, className) =>
      val actorRef =  loadClassFromJar(jarAddress, className)
      actorRef.map(actorRef =>
        {val path = actorRef.path.toString
          externalActorList(path) = actorRef})
    case QueryExternalClass => log.info(externalActorList.toString())
      sender() ! externalActorList.toString()
  }

}
