package hydra.cluster.test.scenario

import akka.actor.{Address}
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import hydra.cluster.ClusterListener.SimpleClusterApp
import hydra.cluster.deploy.DeployService.DeployReq

/**
  * Created by TaoZhou(whereby@live.cn) on 14/10/2017.
  */
object TestNodeFailed {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      val systems = SimpleClusterApp.startup(Seq("2551", "2552", "0" ))
      val osString = System.getProperty("os.name")
      var appConfigString =""
      if(osString.toLowerCase().startsWith("win")){
        appConfigString =
          """
            |{
            | "appname": "appTest",
            | "startcmd":["cmd.exe","/c","python demo/app.py"],
            | "prestartcmd":[]
            |}
          """.stripMargin
      }else{
        appConfigString =
          """
            |{
            | "appname": "appTest",
            | "startcmd":["python demo/app.py"],
            | "prestartcmd":[]
            |}
          """.stripMargin
      }


      val address = Address("akka.tcp", "ClusterSystem", "127.0.0.1", 2551)
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
