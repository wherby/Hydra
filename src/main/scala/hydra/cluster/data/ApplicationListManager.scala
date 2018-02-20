package hydra.cluster.data

import akka.actor.Address


/**
  * Created by TaoZhou(whereby@live.cn) on 13/10/2017.
  */
object ApplicationListManager{
    import scala.collection.mutable.Map
    var applicationListMap :Map[Address,ApplicationList] = Map()
    var externaActorListMap :Map[Address, ApplicationList] =Map()
    def getApplicationList(address:Address)={
    applicationListMap.get(address) match {
      case None => val applicationlist = new ApplicationList()
        applicationListMap = applicationListMap + (address -> applicationlist)
        applicationlist
      case Some(applicationList) => applicationList
    }
  }
  def getExternalList(address:Address)={
    externaActorListMap.get(address) match {
      case None => val applicationlist = new ApplicationList()
        externaActorListMap = externaActorListMap + (address -> applicationlist)
        applicationlist
      case Some(applicationList) => applicationList
    }
  }
}
