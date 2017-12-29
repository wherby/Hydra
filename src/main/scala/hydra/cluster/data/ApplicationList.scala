package hydra.cluster.data

import akka.actor.Address
import hydra.cluster.Cons.AppRequst
import hydra.cluster.Log.HydraLogger
import play.api.libs.json.Json


/**
  * Created by TaoZhou(whereby@live.cn) on 25/09/2017.
  */
class ApplicationList extends ApplicationListTrait with AppListTrait with HydraLogger{
  import scala.collection.mutable.Map
  val systemlist:Map[Address,List[String]] = Map()

  def addSystem(address:Address) ={
    systemlist.get(address) match {
      case Some(value) =>
      case None => systemlist(address)= List()
    }
  }

  def removeSystem(address: Address)={
    systemlist.get(address) match {
      case Some(value) => systemlist.remove(address)
      case None =>
    }
    appList.keys.map{
      key => appList(key) = appList(key).filter(_ != address)
    }
  }

  def addApplicationToSystem(address: Address,app:String)={
    systemlist.get(address) match {
      case Some(value)=> systemlist(address) =systemlist(address):::List(app)
      case None => systemlist(address)= List(app)
    }
    val appName = (Json.parse(app) \ AppRequst.appname).asOpt[String]
    appName match {
      case Some(appName) =>appList.get(appName) match {
        case Some(value) => appList(appName) = appList(appName) ::: List(address)
        case _ =>appList(appName)=  List(address)
      }
      case None=>
    }
  }

  def removeApplicationFromSystem(address: Address, app:String)={
    systemlist.get(address) match {
      case Some(value) => systemlist(address) = systemlist(address).filter(_ != app)
      case None =>systemlist(address) =  List()
    }
    val appName = (Json.parse(app) \ AppRequst.appname).asOpt[String]
    appName match {
      case Some(appName) => appList.get(appName) match {
        case Some(value) => appList(appName) =appList(appName).filter(_ !=address)
        case None => appList(appName)=  List()
      }
      case _=>
    }
  }

  def getApplication():String={
    systemlist.toString()
  }
}

object ApplicationList {
  def main(args: Array[String]): Unit = {

  }
}
