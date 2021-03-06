package hydra.cluster.test

import akka.actor.Address
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import hydra.cluster.app.SimpleClusterApp
import hydra.cluster.eventlistener.Aggregator.FailedMsgReport

/**
  * Created by TaoZhou(whereby@live.cn) on 13/10/2017.
  */
object AggregatorTest {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      val systems = SimpleClusterApp.startup(Seq("2551", "2552", "0"))
      systems.map{system =>
        val aggregatorProxy = system.actorOf(ClusterSingletonProxy.props(
          singletonManagerPath = "/user/aggregator",
          settings = ClusterSingletonProxySettings(system)),
          name = "aggregatorProxy")
        val add =new Address("akka.tcp","localhost")
        Thread.sleep(1999)
        aggregatorProxy ! FailedMsgReport(add,System.currentTimeMillis())
        println("****** Send Failed Msg*******")
      }
    }
    else
      SimpleClusterApp.startup(args)
  }

}
