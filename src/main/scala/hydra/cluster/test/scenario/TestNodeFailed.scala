package hydra.cluster.test.scenario

import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import hydra.cluster.ClusterListener.SimpleClusterApp
import hydra.cluster.common.DeployService.DeployReq
import hydra.cluster.test.TestCons

/**
  * Created by TaoZhou(whereby@live.cn) on 14/10/2017.
  */
object TestNodeFailed {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      val systems = SimpleClusterApp.startup(Seq("2551", "2552", "0" ))
      val appConfigString =TestCons.pythonTestConfig
      val deployServiceProxy = systems(0).actorOf(ClusterSingletonProxy.props(
        singletonManagerPath = "/user/deployservice",
        settings = ClusterSingletonProxySettings(systems(0))),
        name = "deployserviceProxy2")
      Thread.sleep(10000)
      deployServiceProxy ! DeployReq(appConfigString)
      println("deploy python finished")
      Thread.sleep(10000)
      systems(0).terminate()
      println("teminate one node")
    }
    else
      SimpleClusterApp.startup(args)
  }
}
