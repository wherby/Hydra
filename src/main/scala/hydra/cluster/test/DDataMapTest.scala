package hydra.cluster.test

import akka.actor.Props
import hydra.cluster.app.SimpleClusterApp
import hydra.cluster.data.DDataMap

import scala.util.Random

/**
  * Created by TaoZhou(whereby@live.cn) on 15/10/2017.
  */
object DDataMapTest {
  import hydra.cluster.data.DData._
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      val systems = SimpleClusterApp.simpleStartup(Seq("2551", "2552", "0"))
      Thread.sleep(10000)
      systems.map{system =>
        val ddata = system.actorOf(Props[DDataMap],"ddatamap")
        ddata ! AddValueToKey("abc","a" + system.name + Random.nextInt(1000).toString)
        ddata ! AddValueToKey("ab","a" + system.name + Random.nextInt(1000).toString)
        Thread.sleep(1000)
        println("****** Get Key *******")
        ddata ! GetKey("abc")
        ddata ! GetKey("ab")
        ddata ! GetAllKey("ab")
      }
    }
    else
      SimpleClusterApp.startup(args)
  }
}
