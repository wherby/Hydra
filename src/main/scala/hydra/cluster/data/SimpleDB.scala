package hydra.cluster.data

/**
  * Created by TaoZhou(whereby@live.cn) on 30/09/2017.
  */
class SimpleDB extends DBImplement {

  import scala.collection.mutable.Map

  var records: Map[String, List[String]] = Map()

  def save(key: String, info: String): Unit = {
    records.get(key) match {
      case Some(value) => records(key) = value ::: List(info)
      case _ => records = records + (key -> List(info))
    }
  }

  def listAll(): Map[String, List[String]] = {
    records
  }

  def getKey(key : String) : List[String]={
    records(key)
  }
}


object SimpleDB {
  def main(args: Array[String]): Unit = {
    val db: DBImplement = new SimpleDB()
    db.save("app1", "start")
    db.save("app1", "deploy on c1")
    db.save("app1", "failed on c1")
    val record = db.listAll()
    println(record("app1"))
    println(record)
  }
}