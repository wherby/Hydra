package hydra.cluster.data

/**
  * Created by TaoZhou(whereby@live.cn) on 30/09/2017.
  */
trait DBImplement {

  import scala.collection.mutable.Map

  def save(key: String, info: String): Unit

  def listAll(): Map[String, List[String]]
}
