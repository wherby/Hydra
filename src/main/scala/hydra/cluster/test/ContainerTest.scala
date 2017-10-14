package hydra.cluster.test

import akka.actor.{Address}
import hydra.cluster.container.Container.InitialMsg
import hydra.cluster.deploy.DeployService
import hydra.cluster.ClusterListener.SimpleClusterApp

/**
  * Created by TaoZhou(whereby@live.cn) on 10/10/2017.
  */
object ContainerTest {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      val systems = SimpleClusterApp.startup(Seq("2551", "2552", "0"))
      val address = Address("akka.tcp", "ClusterSystem", "127.0.0.1", 2551)
      val actorRef = DeployService.tryToInstanceDeployActor("hydra.cluster.container.Container", address, systems(0), "ContainerApp")
      val appConfigString =
        """
          |{
          | "appname": "appTest",
          | "startcmd":["cmd.exe","/c","python demo/app.py"],
          | "prestartcmd":["cmd.exe","/c","dir"]
          |}
        """.stripMargin
      actorRef map {
        appref => appref ! InitialMsg(appConfigString)
          Thread.sleep(10)
          appref ! "check"
        //appref ! "done"
      }
      println("Done")
    }
    else
      SimpleClusterApp.startup(args)
  }
}
