package hydra.cluster.test

import akka.actor.Address
import hydra.cluster.data.ApplicationList
import hydra.cluster.scheduler.SimpleScheduler
/**
  * Created by TaoZhou(whereby@live.cn) on 13/10/2017.
  */
object SimpleScheduleTest {
  def main(args: Array[String]): Unit = {
    var applist= new ApplicationList()
    val add =new Address("akka.tcp", "ClusterSystem", "127.0.0.1", 2551)
    val add2 = new Address("akka.tcp", "ClusterSystem", "127.0.0.1", 2552)
    applist.addSystem(add)
    applist.addSystem(add2)
    applist.addApplicationToSystem(add,"app1")
    applist.addApplicationToSystem(add2,"app2")
    val sch =new   SimpleScheduler()
    val addSche = sch.schedule(applist.systemlist,"app1")
    val addSche1 = sch.schedule(applist.systemlist,"app2")
    println(addSche)
    println(addSche1)
  }
}
