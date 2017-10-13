package hydra.cluster.scheduler

import akka.actor.Address

import scala.collection.mutable.Map

/**
  * Created by TaoZhou(whereby@live.cn) on 13/10/2017.
  * Tbe class is an example of the scheduler design which only choose
  * one actor system which the app not running.
  */
class SimpleScheduler extends SchedulerTrait{
  def schedule(systemlist:Map[Address,List[String]], appconfig: String): Address={
    val listWithoutApp = systemlist.filterNot(_._2.contains(appconfig)).toSeq
    val leastAppMap = listWithoutApp.sortBy(_._2.length)
    leastAppMap match {
      case Nil  => systemlist.head._1
      case _=> leastAppMap.head._1
    }
  }
}

