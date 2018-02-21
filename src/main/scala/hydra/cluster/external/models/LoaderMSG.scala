package hydra.cluster.external.models

import akka.actor.Address


/**
  * For hydra.cluster.ExternalLoader in Hydra
  * Created by whereby[Tao Zhou](187225577@qq.com) on 2018/2/16
  */
object LoaderMSG {

  final case class ExternalLoaderRequest(jarAddress: String, className: String, address: Option[String] = None,actorName: Option[String] = None)

  final case class QueryExternalClass()

  final case class QueryChildren(address: Option[String] = None)

  final case class DeleteChildren(actorName: String, address: Option[String] = None)

  final case class DeployExternalActor(address: Address, jarAddress: String, className: String, actorName: String)

  final case class RemoveExternalActor(address: Address, className: String)

  final case class ExternalActorRecord(jarAddress: String, className: String, actorName: String)
}
