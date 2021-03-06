package hydra.cluster.app

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import com.typesafe.config.ConfigFactory
import hydra.cluster.constent.HydraConfig
import hydra.cluster.deploy.DeployService
import hydra.cluster.eventlistener.{Aggregator, SimpleClusterListener}
import hydra.cluster.external.ExternalActorLoader
import hydra.cluster.web.HydraWebServer

/**
  * Created by TaoZhou(whereby@live.cn) on 25/09/2017.
  */

object SimpleClusterApp {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      val port = HydraConfig.load().getString("akka.remote.netty.tcp.port")
      startup(Seq(port))
    }
    else
      startup(args)
  }

  def startup(ports: Seq[String]): Seq[ActorSystem] = {
    val config = HydraConfig.load()

    val clusterSystemName = config.getString("hydra.clustername")
    val systems= ports map { port =>
      // Override the configuration of the port
      val sysConfig = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)
          .withFallback(HydraConfig.load())


      // Create an Akka system
      val system = ActorSystem(clusterSystemName, sysConfig)
      // Create an actor that handles cluster domain events
      system.actorOf(Props[SimpleClusterListener], name = "clusterListener")

      system.actorOf(ClusterSingletonManager.props(
        singletonProps = Props[DeployService],
        terminationMessage = PoisonPill,
        settings = ClusterSingletonManagerSettings(system)),
        name = "deployservice")

      system.actorOf(ClusterSingletonManager.props(
        singletonProps = Props[Aggregator],
        terminationMessage = PoisonPill,
        settings = ClusterSingletonManagerSettings(system)),
        name = "aggregator")

      system.actorOf(Props[ExternalActorLoader],"externalLoader")

      system
    }

    config.getBoolean("hydra.web.enable") match {
      case true => HydraWebServer.createWebServer(systems(0))
      case _=>
    }
    sys.ShutdownHookThread {
      systems.map{
        system =>system.terminate()
      }
    }
    systems
  }
  def simpleStartup(ports: Seq[String]): Seq[ActorSystem] = {

    val systems= ports map { port =>
      // Override the configuration of the port
      val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
        withFallback(HydraConfig.load())
      val clusterSystemName = config.getString("hydra.clustername")
      // Create an Akka system
      val system = ActorSystem(clusterSystemName, config)

      //If topic is not registered to mediator ,send topic message will send to dead letter.
      // Create an actor that handles cluster domain events
      system.actorOf(Props[SimpleClusterListener], name = "clusterListener")


      system
    }

    systems
  }

  def startWeb(systems: Seq[ActorSystem])={
    val config = HydraConfig.load()
    config.getBoolean("hydra.web.enable") match {
      case true => HydraWebServer.createWebServer(systems(0))
      case _=>
    }
  }

  def setupClusterService(system: ActorSystem)={
    system.actorOf(ClusterSingletonManager.props(
      singletonProps = Props[DeployService],
      terminationMessage = PoisonPill,
      settings = ClusterSingletonManagerSettings(system)),
      name = "deployservice")

    system.actorOf(ClusterSingletonManager.props(
      singletonProps = Props[Aggregator],
      terminationMessage = PoisonPill,
      settings = ClusterSingletonManagerSettings(system)),
      name = "aggregator")
  }

}

