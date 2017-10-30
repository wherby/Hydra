package hydra.cluster.test

import akka.actor.{Address, PoisonPill, Props}
import akka.cluster.singleton._
import  hydra.cluster.common.msg.DeployService.DeployReq
import hydra.cluster.ClusterListener.SimpleClusterApp

/**
  * Created by TaoZhou(whereby@live.cn) on 13/10/2017.
  */
object DeployServiceTest {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      val systems = SimpleClusterApp.startup(Seq("2551", "2552", "0"))
      val appConfigString =
        """
          |{
          | "appname": "appTest",
          | "startcmd":["cmd.exe","/c","python demo/app.py"],
          | "prestartcmd":["cmd.exe","/c","dir"]
          |}
        """.stripMargin
      val address = Address("akka.tcp", "ClusterSystem", "127.0.0.1", 2551)
      val deployServiceProxy = systems(0).actorOf(ClusterSingletonProxy.props(
        singletonManagerPath = "/user/deployservice",
        settings = ClusterSingletonProxySettings(systems(0))),
        name = "deployserviceProxy")
      deployServiceProxy ! DeployReq(appConfigString)
      println("deploy python finished")
    }
    else
      SimpleClusterApp.startup(args)
  }
}
