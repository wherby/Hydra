package hydra.cluster.deploy

import akka.actor.{Actor, ActorLogging}
import hydra.cluster.data.ApplicationListManager
import hydra.cluster.deploy.DeployService.DeployRecipe
import hydra.cluster.common.msg.DeployService.DeployReq
import hydra.cluster.scheduler.SchedulerTrait
import play.api.libs.json.Json
import akka.cluster.Cluster
import hydra.cluster.Cons.{AppRequst, HydraConfig}

/**
  * Created by TaoZhou(whereby@live.cn) on 13/10/2017.
  */
class DeployScheduler extends Actor with ActorLogging {
  val config = HydraConfig.load()
  lazy val schedulerClazz: String = config.getString("hydra.scheduler")
  val selfAddress = Cluster(context.system).selfAddress
  lazy val containerClazz: String = config.getString("hydra.container")

  def receive = {
    case DeployReq(appconfig) =>
      val configJson = Json.parse(appconfig)
      val schedulerClass = (configJson \ AppRequst.scheduler).asOpt[String].getOrElse(schedulerClazz)
      val containerClass = (configJson \ AppRequst.container).asOpt[String]
      val scheduler = Class.forName(schedulerClass).newInstance().asInstanceOf[SchedulerTrait]
      val address = scheduler.schedule(ApplicationListManager.getApplicationList(selfAddress).systemlist, appconfig)
      log.info(s"Selct the address: $address")
      address match {
        case Some(address)=>sender() ! DeployRecipe(appconfig, address, containerClass)
        case _=>
      }
  }
}
