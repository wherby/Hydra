package hydra.cluster.test.scenario

import akka.actor.Props
import hydra.cluster.app.SimpleClusterApp
import hydra.cluster.external.ExternalActorLoader
import hydra.cluster.external.models.LoaderMSG.ExternalLoaderRequest

/**
  * For hydra.cluster.test.scenario in Hydra
  * Created by whereby[Tao Zhou](187225577@qq.com) on 2018/2/19
  */
object ExternalAcotorHATest {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      val systems = SimpleClusterApp.simpleStartup(Seq("2551", "2552", "2553"))
      systems.map(system => SimpleClusterApp.setupClusterService(system))
      Thread.sleep(1000)
      systems.map {
        system =>
          val loader = system.actorOf(Props[ExternalActorLoader], "externalLoader")
          loader ! ExternalLoaderRequest("C:\\temp\\a\\ExternalPackage.jar", "hydra.cluster.external.actors.TestActor")
          loader ! ExternalLoaderRequest("C:\\temp\\a\\ExternalPackage.jar", "hydra.cluster.external.actors.TestActor")
          Thread.sleep(1999)
          system.actorSelection("/user/externalLoader/TestActor*").tell("Testaa******************** ", loader)
      }
      SimpleClusterApp.startWeb(systems)
      //Stop Systems(0) to test HA
      Thread.sleep(15000)
      systems(2).terminate()
      Thread.sleep(45000)
      systems(1).terminate()
      println("deploy python finished")
    }
    else
      SimpleClusterApp.startup(args)
  }
}
