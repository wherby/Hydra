package hydra.cluster.test

import akka.actor.Address
import hydra.cluster.app.SimpleClusterApp
import hydra.cluster.container.Container.InitialMsg
import hydra.cluster.deploy.DeployService
import hydra.cluster.constent.HydraConfig

/**
  * Created by TaoZhou(whereby@live.cn) on 10/10/2017.
  */
object ContainerTest {
  def main(args: Array[String]): Unit = {
    val config = HydraConfig.load()
    val systemname =config.getString("hydra.clustername")
    if (args.isEmpty) {
      val systems = SimpleClusterApp.startup(Seq("2551", "2552", "2553"))
      val address = Address("akka.tcp", systemname, "127.0.0.1", 2551)
      val actorRef = DeployService.tryToInstanceDeployActor("hydra.cluster.container.Container", address, systems(0), "ContainerApp")
      val appConfigString =TestCons.pythonTestConfig

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
