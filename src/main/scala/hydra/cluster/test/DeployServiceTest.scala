package hydra.cluster.test

import akka.cluster.singleton._
import hydra.cluster.app.SimpleClusterApp
import hydra.cluster.common.DeployService.DeployReq

/**
  * Created by TaoZhou(whereby@live.cn) on 13/10/2017.
  */
object DeployServiceTest {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      val systems = SimpleClusterApp.startup(Seq("2551", "2552", "0"))
      val appConfigString =TestCons.pythonTestConfig
      val deployServiceProxy = systems(0).actorOf(ClusterSingletonProxy.props(
        singletonManagerPath = "/user/deployservice",
        settings = ClusterSingletonProxySettings(systems(0))),
        name = "deployserviceProxy2")
      deployServiceProxy ! DeployReq(appConfigString)
      println("deploy python finished")
    }
    else
      SimpleClusterApp.startup(args)
  }
}
