package hydra.cluster.ClusterListener

import com.typesafe.config.ConfigFactory
import akka.actor.{ActorSystem, Props}

/**
  * Created by TaoZhou(whereby@live.cn) on 25/09/2017.
  */

object SimpleClusterApp {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      val systems = startup(Seq("2551", "2552", "0"))
      Thread.sleep(10000)
      systems(0).terminate()
    }
    else
      startup(args)
  }

  def startup(ports: Seq[String]): Seq[ActorSystem] = {
    ports map { port =>
      // Override the configuration of the port
      val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
        withFallback(ConfigFactory.load())

      // Create an Akka system
      val system = ActorSystem("ClusterSystem", config)
      // Create an actor that handles cluster domain events
      system.actorOf(Props[SimpleClusterListener], name = "clusterListener")

      system
    }
  }
}

