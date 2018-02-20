package hydra.cluster.data

import akka.actor.Address

/**
  * For hydra.cluster.data in Hydra
  * Created by whereby[Tao Zhou](187225577@qq.com) on 2018/2/18
  */
trait ExternalActorListTrait extends  ApplicationListTrait{

  def getApplicationList(address: Address):List[String]
}
