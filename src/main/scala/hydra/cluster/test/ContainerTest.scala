package hydra.cluster.test

import akka.actor.Address
import hydra.cluster.container.Container.InitialMsg
import hydra.cluster.deploy.DeployService
import hydra.cluster.ClusterListener.SimpleClusterApp

/**
  * Created by TaoZhou(whereby@live.cn) on 10/10/2017.
  */
object ContainerTest {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      val systems = SimpleClusterApp.startup(Seq("2551", "2552", "2553"))
      val address = Address("akka.tcp", "ClusterSystem", "127.0.0.1", 2551)
      val actorRef = DeployService.tryToInstanceDeployActor("hydra.cluster.container.Container", address, systems(0), "ContainerApp")
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
      Thread.sleep(1000)
      actorRef map {
        appref => appref ! InitialMsg(appConfigString)
          appref ! "check"
        //appref ! "done"
      }
      println("Done")
    }
    else
      SimpleClusterApp.startup(args)
  }
}
