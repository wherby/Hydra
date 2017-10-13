package hydra.cluster.data

import akka.actor.{Actor, ActorLogging, Address}
import akka.cluster.client.ClusterClient.Publish
import akka.cluster.pubsub.DistributedPubSub
import hydra.cluster.data.Aggregator.FailedMsg

/**
  * Created by TaoZhou(whereby@live.cn) on 02/10/2017.
  */
class Aggregator extends Actor with ActorLogging {

  import scala.collection.mutable.Map

  var failedNode: Map[Address, Long] = Map()
  val maxDelay = 10000

  val mediator = DistributedPubSub(context.system).mediator

  def receive = {
    case FailedMsg(address, time) => failedNode.get(address) match {
      case None if System.currentTimeMillis() - time < maxDelay => failedNode = failedNode + (address -> time)
        log.info("Publish hydra.nodeFailed Message for :" + address.toString)
        mediator ! Publish("hydra.nodeFailed", FailedMsg(address, time))
      case _ =>
    }
  }

}

object Aggregator {

  case class FailedMsg(address: Address, time: Long)

}
