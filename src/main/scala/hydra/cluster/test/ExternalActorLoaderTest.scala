package hydra.cluster.test


import akka.actor.Props
import hydra.cluster.ClusterListener.SimpleClusterApp
import hydra.cluster.external.ExternalActorLoader
import hydra.cluster.external.models.LoaderMSG.{ExternalLoaderRequest, QueryExternalClass}


/**
  * For hydra.cluster.test in Hydra
  * Created by whereby[Tao Zhou](187225577@qq.com) on 2018/2/16
  */
object ExternalActorLoaderTest {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      val systems = SimpleClusterApp.simpleStartup(Seq("2551", "2552", "0"))
      systems.map {
        system =>
          val loader = system.actorOf(Props[ExternalActorLoader], "externalLoader")
          loader ! ExternalLoaderRequest("C:\\temp\\a\\ExternalPackage.jar", "hydra.cluster.external.actors.TestActor")
          loader ! ExternalLoaderRequest("C:\\temp\\a\\ExternalPackage.jar", "hydra.cluster.external.actors.TestActor")
          Thread.sleep(1999)
          val TestActor = system.actorSelection("/user/externalLoader/TestActor*").tell("Testaa******************** ", loader)
          loader ! QueryExternalClass
      }
      println("deploy python finished")
    }
    else
      SimpleClusterApp.startup(args)
  }
}
