package hydra.cluster.test

import akka.actor.Address
import hydra.cluster.constent.HydraConfig
import hydra.cluster.data.ApplicationList
import hydra.cluster.scheduler.SimpleScheduler

/**
  * Created by TaoZhou(whereby@live.cn) on 13/10/2017.
  */
object SimpleScheduleTest {
  def main(args: Array[String]): Unit = {
    val config = HydraConfig.load()
    val systemname =config.getString("hydra.clustername")
    val applist = new ApplicationList()
    val add = new Address("akka.tcp", systemname, "127.0.0.1", 2551)
    val add2 = new Address("akka.tcp", systemname, "127.0.0.1", 2552)
    applist.addSystem(add)
    applist.addSystem(add2)
    applist.addApplicationToSystem(add, TestCons.pythonTestConfig)
    applist.addApplicationToSystem(add2, TestCons.pythonTestConfig2)
    val sch = new SimpleScheduler()
    val addSche = sch.schedule(applist.systemlist, TestCons.pythonTestConfig)
    val addSche1 = sch.schedule(applist.systemlist, TestCons.pythonTestConfig2)
    println(addSche)
    println(addSche1)
    println(applist.systemlist)
    println(applist.appList)
  }
}
