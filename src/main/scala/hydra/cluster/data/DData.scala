package hydra.cluster.data

/**
  * Created by TaoZhou(whereby@live.cn) on 15/10/2017.
  */
object DData {

  case class AddValueToKey(key: String, value: String)

  case class RemoveValueFromKey(key: String, value: String)

  case class GetKey(key: String)

  case class AddKey(key: String)

  case class RemoveKey(key: String)

}
