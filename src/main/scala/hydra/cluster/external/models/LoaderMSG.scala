package hydra.cluster.external.models

/**
  * For hydra.cluster.ExternalLoader in Hydra
  * Created by whereby[Tao Zhou](187225577@qq.com) on 2018/2/16
  */
object LoaderMSG {
  final case class ExternalLoaderRequest(jarAddress: String, className: String,address:Option[String] = None)
  final case class QueryExternalClass()
  final case class QueryChilderen()
}
