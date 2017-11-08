package hydra.cluster.scheduler

import akka.actor.Address
import scala.collection.mutable.Map
import scala.util.Random

/**
  * Created by TaoZhou(whereby@live.cn) on 13/10/2017.
  * Tbe class is an example of the scheduler design which only choose
  * one actor system which the app not running.
  */
class SimpleScheduler extends SchedulerTrait {
  def schedule(systemlist: Map[Address, List[String]], appconfig: String): Option[Address] = {
    val listWithoutApp = systemlist.filterNot(_._2.contains(appconfig)).toSeq
    listWithoutApp match {
      case Nil => None
      case _ => Some(listWithoutApp(Random.nextInt(listWithoutApp.length))._1)
    }
  }
}

