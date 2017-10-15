package hydra.cluster.container

import akka.actor.Actor
import hydra.cluster.container.Container.StartCmd
import scala.sys.process._
/**
  * Created by TaoZhou(whereby@live.cn) on 14/10/2017.
  */
class Runner extends Actor{
  def receive ={
    case StartCmd(startcmds) => startcmds.!
  }
}
