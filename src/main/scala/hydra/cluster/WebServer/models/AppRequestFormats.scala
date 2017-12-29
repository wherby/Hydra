package hydra.cluster.WebServer.models
import play.api.libs.json._

/**
  * For hydra.cluster.WebServer.models in Hydra
  * Created by whereby[Tao Zhou](187225577@qq.com) on 2017/12/25
  */
trait AppRequestFormats {

  implicit object AppRequestFormat extends Format[AppRequest] {
    override def reads(json: JsValue): JsSuccess[AppRequest] = JsSuccess(AppRequest(
      (json \ "appname").as[String],
      (json \ "startcmd").as[Seq[String]]
    ))

    override def writes(apprequest: AppRequest): JsValue = JsObject(List(
      "appname" -> JsString(apprequest.appname),
      "startcmd" -> JsArray(apprequest.startcmd.map { va => JsString(va) })
    ))
  }

}
