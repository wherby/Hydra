package hydra.cluster.test

import akka.actor.Address
import hydra.cluster.ClusterListener.SimpleClusterApp
import hydra.cluster.deploy.DeployService
import hydra.cluster.deploy.DeployService.DeployedMsg

/**
  * For hydra-cluster-scala
  * Created by TaoZhou(187225577@qq.com) on 25/10/2017. 
  */
object InstanceSericeTest {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      val systems = SimpleClusterApp.startup(Seq("2551", "2552", "0"))
      val address = Address("akka.tcp", "ClusterSystem", "127.0.0.1", 2551)
      val actorRef = DeployService.tryToInstanceDeployActor("hydra.cluster.deploy.DeployService", address, systems(0), "aa")
      actorRef.map {
        actorref => actorref ! DeployedMsg(address, "CCCCCC")
      }
    }
    else
      SimpleClusterApp.startup(args)
  }
}
