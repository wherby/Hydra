package hydra.cluster.WebServer.models
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
/**
  * For hydra.cluster.WebServer.models in Hydra
  * Created by whereby[Tao Zhou](187225577@qq.com) on 2017/12/26
  */
trait AppRequestJsonFormat  extends SprayJsonSupport with DefaultJsonProtocol{
  implicit  val appRequestFormat = jsonFormat2(AppRequest)
}
