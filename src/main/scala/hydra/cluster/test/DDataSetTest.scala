package hydra.cluster.test

import akka.actor.Props
import hydra.cluster.app.SimpleClusterApp
import hydra.cluster.data.DDataSet

import scala.util.Random

/**
  * Created by TaoZhou(whereby@live.cn) on 15/10/2017.
  */
object DDataSetTest {
  import hydra.cluster.data.DData._
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      val systems = SimpleClusterApp.simpleStartup(Seq("2551", "2552", "0"))
      Thread.sleep(1900)
      systems.map{system =>
        val ddata = system.actorOf(Props[DDataSet],"ddata")
        ddata ! AddValueToKey("abc","a" + system.name + Random.nextInt(1000).toString)
        Thread.sleep(1000)
        println("****** Get Key *******")
        ddata ! GetKey("abc")
      }
    }
    else
      SimpleClusterApp.startup(args)
  }
}
