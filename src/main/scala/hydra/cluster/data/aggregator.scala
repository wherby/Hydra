package hydra.cluster.data
import akka.actor.{Actor, ActorLogging, Address, PoisonPill, Props}
import akka.cluster.client.ClusterClient.Publish
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import hydra.cluster.data.Aggregator.FailedMsg

/**
  * Created by TaoZhou(whereby@live.cn) on 02/10/2017.
  */
class Aggregator extends Actor with ActorLogging{
  import scala.collection.mutable.Map
  var failedNode:Map[Address,Long]  = Map()
  val maxDelay = 10000

  val mediator = DistributedPubSub(context.system).mediator
  def receive={
    case FailedMsg(address,time) => failedNode.get(address) match {
      case None if System.currentTimeMillis() - time < maxDelay => failedNode = failedNode + (address -> time)
        log.info("Publish hydra.nodeFailed Message for :" + address.toString)
        mediator ! Publish("hydra.nodeFailed",FailedMsg(address,time))
      case _=>
    }
  }

}

object Aggregator{
  case class FailedMsg(address: Address,time:Long)
  import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
  import hydra.cluster.simple.SimpleClusterApp
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      val systems = SimpleClusterApp.startup(Seq("2551", "2552", "0"))
      systems.map{system =>system.actorOf(ClusterSingletonManager.props(
        singletonProps = Props[Aggregator],
        terminationMessage = PoisonPill,
        settings = ClusterSingletonManagerSettings(system)),
        name = "aggregator")
        val aggregatorProxy = system.actorOf(ClusterSingletonProxy.props(
          singletonManagerPath = "/user/aggregator",
          settings = ClusterSingletonProxySettings(system)),
          name = "aggregatorProxy")
        val add =new Address("akka.tcp","localhost")
        Thread.sleep(1999)
        aggregatorProxy ! FailedMsg(add,System.currentTimeMillis())
        println("****** Send Failed Msg*******")
      }
    }
    else
      SimpleClusterApp.startup(args)
  }

}
