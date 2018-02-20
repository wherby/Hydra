package hydra.cluster.external.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import hydra.cluster.external.models.LoaderMSG.QueryChildren
import spray.json.DefaultJsonProtocol

/**
  * For hydra.cluster.external.models in Hydra
  * Created by whereby[Tao Zhou](187225577@qq.com) on 2018/2/18
  */
trait QueryChildrenJsonFormat extends SprayJsonSupport with DefaultJsonProtocol{
  implicit  val queryChildrenRequestFormat = jsonFormat1(QueryChildren)
}
