package hydra.cluster.data

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.{DistributedData, ORMultiMap,ORMultiMapKey}


import scala.concurrent.duration._

/**
  * Created by TaoZhou(whereby@live.cn) on 15/10/2017.
  */

class DDataMap extends Actor with ActorLogging {

  import DData._
  import scala.collection.mutable.Map

  implicit val cluster = Cluster(context.system)
  val replicator = DistributedData(context.system).replicator
  val timeout = 3.seconds
  val readMajority = ReadMajority(timeout)
  val writeMajority = WriteMajority(timeout)
  var m1111 =Map[String, Set[String]]()

  def receive = receiveGet
    .orElse[Any, Unit](receiveAddItem)
    .orElse[Any, Unit](removeItem)
    .orElse[Any, Unit](addKeyToMap)
    .orElse[Any, Unit](removeKeyFromMap)

  def receiveGet: Receive = {
    case GetKey(key) =>
      val keyData = ORMultiMapKey[String, String](key)
      replicator ! Get(keyData, readMajority, Some(sender()))
    case g@GetSuccess(keyData, Some(replyTo: ActorRef)) =>
      val data = g.get(keyData)
      log.info(s"get data : $data")
      replyTo ! data
    case NotFound(keyData, Some(replyTo: ActorRef)) =>
      replyTo ! List()
    case GetFailure(keyData, Some(replyTo: ActorRef)) =>
      replicator ! Get(keyData, ReadLocal, Some(replyTo))
  }

  def receiveAddItem: Receive = {
    case cmd@AddValueToKey(key, value) =>
      val keyData = ORMultiMapKey[String, String](key)
      val update = Update(keyData, ORMultiMap.empty[String, String], writeMajority, Some(cmd)) {
        lst => updateSeqAdd(lst, key, value)
      }
      replicator ! update
  }

  def updateSeqAdd(lst: ORMultiMap[String,String], key: String, value: String): ORMultiMap[String, String] = {
    println(s"lst: $lst, $value")
    lst.get(key) match {
      case Some(seqStr) => val seq = seqStr + value
        lst + (key -> seq)
      case None => lst + (key ->Set(value))
    }
  }

  def removeItem: Receive = {
    case cmd@RemoveValueFromKey(key, value) =>
      val keyData = ORMultiMapKey[String, String](key)
      val update = Update(keyData, ORMultiMap.empty[String, String], writeMajority, Some(cmd)) {
        lst => updateSeqRemove(lst, key, value)
      }
      replicator ! update
  }

  def updateSeqRemove(lst: ORMultiMap[String, String], key: String, value: String): ORMultiMap[String, String] = {
    lst.get(key) match {
      case Some(seqStr) => val seq = seqStr -value
        lst + (key ->seq)
      case None => lst + (key -> Set())
    }
  }

  def addKeyToMap: Receive = {
    case cmd@AddKey(key) =>
      val keyData = ORMultiMapKey[String, String](key)
      val update = Update(keyData, ORMultiMap.empty[String, String], writeMajority, Some(cmd)) {
        lst => updateSeqAddEmpty(lst, key)
      }
      replicator ! update
  }

  def updateSeqAddEmpty(lst: ORMultiMap[String,String], key: String): ORMultiMap[String, String] = {
    lst.get(key) match {
      case Some(seqStr) => lst
      case None => lst + (key ->Set())
    }
  }

  def removeKeyFromMap: Receive = {
    case cmd@RemoveKey(key) =>
      val keyData = ORMultiMapKey[String, String](key)
      val update = Update(keyData, ORMultiMap.empty[String, String], writeMajority, Some(cmd)) {
        lst => lst - key
      }
      replicator ! update
  }

}
