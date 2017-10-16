package hydra.cluster.ClusterListener

import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.actor.{Actor, ActorLogging}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import hydra.cluster.ClusterListener.Aggregator.FailedMsgReport
import hydra.cluster.data.{ApplicationListManager, ApplicationListTrait}
import hydra.cluster.deploy.DeployService.{DeployedMsg, UnDeployMsg}

/**
  * Created by TaoZhou(whereby@live.cn) on 25/09/2017.
  */

class SimpleClusterListener extends Actor with ActorLogging {

  import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe

  val cluster = Cluster(context.system)
  val selfAddress = Cluster(context.system).selfAddress
  val applicationList :ApplicationListTrait = ApplicationListManager.getApplicationList(selfAddress)
  val mediator = DistributedPubSub(context.system).mediator
  val aggregatorProxy = context.system.actorOf(ClusterSingletonProxy.props(
    singletonManagerPath = "/user/aggregator",
    settings = ClusterSingletonProxySettings(context.system)),
    name = "aggregatorProxy")
  mediator ! Subscribe(HydraTopic.deployedMsg, self)

  // subscribe to cluster changes, re-subscribe when restart 
  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])

  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case MemberUp(member) =>
      applicationList.addSystem(member.address)
      log.info(applicationList.getApplication())
      log.info("Member is Up: {}", member.address)
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      aggregatorProxy ! FailedMsgReport(member.address, System.currentTimeMillis())
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
    case DeployedMsg(address, app) =>
      applicationList.addApplicationToSystem(address, app)
      log.info("Member Status after deploy: {}", applicationList.getApplication())
    case UnDeployMsg(address, app) =>
      applicationList.removeApplicationFromSystem(address, app)
      log.info("Member Status after undeploy: {}", applicationList.getApplication())
    case _: MemberEvent => // ignore
  }
}