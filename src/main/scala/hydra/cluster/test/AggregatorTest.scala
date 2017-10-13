package hydra.cluster.test

import akka.actor.{Address, PoisonPill, Props}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}
import hydra.cluster.data.Aggregator
import hydra.cluster.data.Aggregator.FailedMsg
import hydra.cluster.ClusterListener.SimpleClusterApp

/**
  * Created by TaoZhou(whereby@live.cn) on 13/10/2017.
  */
object AggregatorTest {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      val systems = SimpleClusterApp.startup(Seq("2551", "2552", "0"))
      systems.map{system =>system.actorOf(ClusterSingletonManager.props(
        singletonProps = Props[Aggregator],
        terminationMessage = PoisonPill,
        settings = ClusterSingletonManagerSettings(system)),
        name = "aggregator")
        val aggregatorProxy = system.actorOf(ClusterSingletonProxy.props(
          singletonManagerPath = "/user/aggregator",
          settings = ClusterSingletonProxySettings(system)),
          name = "aggregatorProxy")
        val add =new Address("akka.tcp","localhost")
        Thread.sleep(1999)
        aggregatorProxy ! FailedMsg(add,System.currentTimeMillis())
        println("****** Send Failed Msg*******")
      }
    }
    else
      SimpleClusterApp.startup(args)
  }

}
