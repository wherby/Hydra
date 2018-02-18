package hydra.cluster.eventlistener

import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.actor.{Actor, ActorLogging}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import hydra.cluster.eventlistener.Aggregator.FailedMsgReport
import hydra.cluster.constent.{HydraTopic, Roles}
import hydra.cluster.data.{ApplicationListManager, ApplicationListTrait, ExternalActorListTrait}
import hydra.cluster.deploy.DeployService.{DeployedMsg, UnDeployMsg}
import hydra.cluster.external.models.LoaderMSG.{DeployExternalActor, ExternalActorRecord, RemoveExternalActor}
import play.api.libs.json.Json

/**
  * Created by TaoZhou(whereby@live.cn) on 25/09/2017.
  */

class SimpleClusterListener extends Actor with ActorLogging{

  import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe

  val cluster = Cluster(context.system)
  val selfAddress = Cluster(context.system).selfAddress
  val applicationList: ApplicationListTrait = ApplicationListManager.getApplicationList(selfAddress)
  val externalActorList: ExternalActorListTrait = ApplicationListManager.getExternalList(selfAddress)
  val mediator = DistributedPubSub(context.system).mediator
  val aggregatorProxy = context.system.actorOf(ClusterSingletonProxy.props(
    singletonManagerPath = "/user/aggregator",
    settings = ClusterSingletonProxySettings(context.system)),
    name = "aggregatorProxy")
  mediator ! Subscribe(HydraTopic.deployedMsg, self)
  //Subscribe external actor message
  mediator ! Subscribe(HydraTopic.deployExternalActor,self)
  implicit val externalActorRecordFormat = Json.format[ExternalActorRecord]
  // subscribe to cluster changes, re-subscribe when restart 
  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])

  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case MemberUp(member) =>
      if (member.roles.contains(Roles.HydraRole)) {
        applicationList.addSystem(member.address)
        log.info(applicationList.getApplication())
        log.info("Member is Up: {}", member.address)
      }
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      if (member.roles.contains(Roles.HydraRole)) {
        aggregatorProxy ! FailedMsgReport(member.address, System.currentTimeMillis())
        log.info("Member is Removed: {} after {}",
          member.address, previousStatus)
      }
    case DeployedMsg(address, app) =>
      applicationList.addApplicationToSystem(address, app)
      log.info("Member Status after deploy: {}", applicationList.getApplication())
    case UnDeployMsg(address, app) =>
      applicationList.removeApplicationFromSystem(address, app)
      log.info("Member Status after undeploy: {}", applicationList.getApplication())
    // For ExternalActor Event:
    case DeployExternalActor(address,jarAddress,className,actorName)=>
      val external = Json.toJson(ExternalActorRecord(jarAddress,className,actorName)).toString()
      log.info(s"Record DeployExternal Actor to :$address with $external")
      externalActorList.addApplicationToSystem(address,external)
    case RemoveExternalActor(address,actorMame)=>
      val allList = externalActorList.getApplicationList(address)
      allList.map{
        actorStr=> val actorJson = (Json.parse(actorStr) \ "actorName").asOpt[String] map {
          actorNameStr=> if(actorMame == actorNameStr) {
            log.info(s"Remove $actorMame from record at $address")
            externalActorList.removeApplicationFromSystem(address,actorStr)
            log.info("After remove record is : " + externalActorList.getApplicationList(address))
          }
        }
      }
    case _: MemberEvent => // ignore
  }
}