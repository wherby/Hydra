package hydra.cluster.data

import akka.actor.Address

import scala.collection.mutable.Map

/**
  * Created by TaoZhou(whereby@live.cn) on 13/10/2017.
  */
trait ApplicationListTrait {
  def addSystem(address: Address): Map[Address, List[String]]

  def removeSystem(address: Address): Map[Address, List[String]]

  def addApplicationToSystem(address: Address, app: String): Map[Address, List[String]]

  def removeApplicationFromSystem(address: Address, app: String): Map[Address, List[String]]

  def getApplication(): String
}
