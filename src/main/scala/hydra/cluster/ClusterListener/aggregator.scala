package hydra.cluster.ClusterListener

import akka.actor.{Actor, ActorLogging, Address}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import com.typesafe.config.ConfigFactory
import hydra.cluster.ClusterListener.Aggregator.{FailedMsg, FailedMsgReport}
import hydra.cluster.Cons.HydraTopic

/**
  * Created by TaoZhou(whereby@live.cn) on 02/10/2017.
  */
class Aggregator extends Actor with ActorLogging {

  import scala.collection.mutable.Map

  var failedNode: Map[Address, Long] = Map()
  val config = ConfigFactory.load()
  val maxDelay: Int = config.getInt("hydra.aggregator.MaxDelay")

  val mediator = DistributedPubSub(context.system).mediator
  override def preStart(): Unit = {
    mediator ! Subscribe(HydraTopic.nodeFail, self)
  }

  val deployServiceProxy = context.system.actorOf(ClusterSingletonProxy.props(
    singletonManagerPath = "/user/deployservice",
    settings = ClusterSingletonProxySettings(context.system)),
    name = "deployserviceProxy")


  def receive = {
    case FailedMsgReport(address, time) => failedNode.get(address) match {
      case None if System.currentTimeMillis() - time < maxDelay => failedNode = failedNode + (address -> time)
        log.info("Publish hydra.nodeFailed Message for :" + address.toString)
        deployServiceProxy !  FailedMsg(address, time)
      case _ =>
    }
  }
}

object Aggregator {

  case class FailedMsg(address: Address, time: Long)

  case class FailedMsgReport(address: Address, time: Long)

}
