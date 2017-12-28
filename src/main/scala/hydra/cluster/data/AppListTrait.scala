package hydra.cluster.data

import akka.actor.Address

import scala.collection.mutable.Map

/**
  * For hydra.cluster.data in Hydra
  * Created by whereby[Tao Zhou](187225577@qq.com) on 2017/12/28
  */
trait AppListTrait {
  val appList:Map[String,List[Address]] = Map()
}
