package hydra.cluster.data

import akka.actor.{ActorRef, Address}

/**
  * Created by TaoZhou(whereby@live.cn) on 16/10/2017.
  */
class ApplicationListDData(ddataRef: ActorRef) extends ApplicationListTrait {
  import DData._
  val AllStytem = "allSystem"
  def addSystem(address: Address)={
    ddataRef ! AddKey(address.toString)
    ddataRef ! AddValueToKey(AllStytem,address.toString)
  }

  def removeSystem(address: Address)={
    ddataRef ! RemoveKey(address.toString)
    ddataRef ! RemoveValueFromKey(AllStytem,address.toString)
  }

  def addApplicationToSystem(address: Address, app: String)={
    ddataRef ! AddValueToKey(address.toString,app)
  }

  def removeApplicationFromSystem(address: Address, app: String)={
    ddataRef ! RemoveValueFromKey(address.toString, app)
  }

  def getApplication(): String={
    "mmm"
  }
}
