package hydra.cluster.simple

import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import hydra.cluster.deploy.DeployService
import hydra.cluster.deploy.DeployService.{Deploy, DeployMsg, UnDeployMsg}

/**
  * Created by TaoZhou(whereby@live.cn) on 25/09/2017.
  */

class SimpleClusterListener extends Actor with ActorLogging {

  import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe

  val cluster = Cluster(context.system)
  val applicationList = new ApplicationList()


  // subscribe to cluster changes, re-subscribe when restart 
  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
    val mediator = DistributedPubSub(context.system).mediator
    mediator ! Subscribe("deploy", self)
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case MemberUp(member) =>
      applicationList.addSystem(member.address)
      log.info(applicationList.getApplication())
      log.info("Member is Up: {}", member.address)
    case UnreachableMember(member) =>
      applicationList.removeSystem(member.address)
      log.info(applicationList.getApplication())
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
    case DeployMsg(address, app) =>
      applicationList.addApplicationToSystem(address, app)
      log.info("Member Status after deploy: {}", applicationList.getApplication())
    case UnDeployMsg(address, app) =>
      applicationList.removeApplicationFromSystem(address, app)
      log.info("Member Status after deploy: {}", applicationList.getApplication())
    case _: MemberEvent => // ignore
  }
}