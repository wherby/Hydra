package hydra.cluster.data

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.{DistributedData, ORSet, ORSetKey}
import scala.concurrent.duration._

/**
  * Created by TaoZhou(whereby@live.cn) on 15/10/2017.
  */
class DDataSet extends Actor with ActorLogging {

  import DData._


  val replicator = DistributedData(context.system).replicator
  implicit val node = Cluster(context.system)

  //implicit val cluster = Cluster(context.system)
  val timeout = 3.seconds
  val readMajority = ReadMajority(timeout)
  val writeMajority = WriteMajority(timeout)
  var allSubscribedKey = Set[ORSetKey[String]]()

  def addKeyToSubscribe(key: ORSetKey[String])={
    if(!allSubscribedKey.contains(key)){
      allSubscribedKey= allSubscribedKey +  key
     // replicator ! Subscribe(key,self)
      //Thread.sleep(1000)
      log.info("Subscribe  a key" )
    }
  }

  def receive = {
    case AddValueToKey(key, value) => log.info(s"add $value to $key")
      val dataKey = ORSetKey[String](key)
      replicator ! Update(dataKey, ORSet.empty[String], writeMajority)(_ + value)
    case RemoveValueFromKey(key, value) => log.info(s"remove $value from $key")
      val dataKey = ORSetKey[String](key)
      replicator ! Update(dataKey, ORSet.empty[String], writeMajority)(_ - value)
    case GetKey(key) => log.info(s"Get Key $key")
      val dataKey = ORSetKey[String](key)
      val sendTo = sender
      replicator ! Get(dataKey,readMajority,Some(sendTo))
    case g@GetSuccess(dataKey,Some(replyTo)) =>
      val data = g.get(dataKey).toString
      log.info(s"the value is  $data")
    case NotFound(_,Some(replyTo)) =>
    case GetFailure(dataKey, Some(replyTo)) =>
      replicator ! Get(dataKey,ReadLocal, Some(replyTo))
    case _=>log.info("Some message received")
  }

}
