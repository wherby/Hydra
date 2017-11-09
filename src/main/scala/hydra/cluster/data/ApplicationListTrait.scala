package hydra.cluster.data

import akka.actor.Address


/**
  * Created by TaoZhou(whereby@live.cn) on 13/10/2017.
  */
trait ApplicationListTrait {
  def addSystem(address: Address)

  def removeSystem(address: Address)

  def addApplicationToSystem(address: Address, app: String)

  def removeApplicationFromSystem(address: Address, app: String)

  def getApplication(): String
}
