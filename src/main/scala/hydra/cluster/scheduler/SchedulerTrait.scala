package hydra.cluster.scheduler

import akka.actor.Address

import scala.collection.mutable.Map

/**
  * Created by TaoZhou(whereby@live.cn) on 13/10/2017.
  */
trait SchedulerTrait {
  def schedule(systemlist: Map[Address, List[String]], appconfig: String): Option[Address]
}
