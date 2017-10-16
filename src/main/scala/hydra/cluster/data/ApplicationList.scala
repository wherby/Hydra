package hydra.cluster.data

import akka.actor.Address


/**
  * Created by TaoZhou(whereby@live.cn) on 25/09/2017.
  */
class ApplicationList extends ApplicationListTrait{
  import scala.collection.mutable.Map
  var systemlist:Map[Address,List[String]] = Map()
  def addSystem(address:Address) ={
    systemlist.get(address) match {
      case Some(value) => systemlist
      case None => systemlist= systemlist +(address-> List());systemlist
    }
  }

  def removeSystem(address: Address)={
    systemlist.get(address) match {
      case Some(value) => systemlist.remove(address);systemlist
      case None =>systemlist
    }
  }

  def addApplicationToSystem(address: Address,app:String)={
    systemlist.get(address) match {
      case Some(value)=> systemlist(address) =systemlist(address):::List(app);systemlist
      case None => systemlist +(address->List(app))
    }
  }

  def removeApplicationFromSystem(address: Address, app:String)={
    systemlist.get(address) match {
      case Some(value) => systemlist(address) = systemlist(address).filter(_ != app);systemlist
      case None => systemlist + (address-> List())
    }
  }

  def getApplication():String={
    systemlist.toString()
  }
}

object ApplicationList {
  def main(args: Array[String]): Unit = {
    val applist= new ApplicationList()
    val add =new Address("akka.tcp","localhost")
    applist.addSystem(add)
    applist.addApplicationToSystem(add,"app1")
    applist.addApplicationToSystem(add,"app2")
    println(applist.getApplication())
    applist.removeApplicationFromSystem(add,"app1")
    println(applist.getApplication())
  }
}
