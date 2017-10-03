package hydra.cluster.deploy

import akka.actor.{Actor, Address}
import akka.cluster.pubsub.DistributedPubSub
import hydra.cluster.deploy.DeployService.{Deploy, DeployMsg, UnDeploy, UnDeployMsg}

/**
  * Created by TaoZhou(whereby@live.cn) on 26/09/2017.
  */
object DeployService {

  final case class Deploy(appName: String, bashString: String)

  final case class DeployMsg(address: Address, app: String)

  final case class UnDeploy(appName: String, bashString: String)

  final case class UnDeployMsg(system: Address,app: String)

}

class DeployService extends Actor {

  import akka.cluster.pubsub.DistributedPubSubMediator.Publish

  val mediator = DistributedPubSub(context.system).mediator

  def receive = {
    case DeployMsg(system,app) =>
      mediator ! Publish("deploy", DeployMsg(system,app))
    case UnDeployMsg(system,app) =>
      mediator ! Publish("deploy",UnDeployMsg(system,app))
    case _ =>
  }
}
