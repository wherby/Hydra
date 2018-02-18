package hydra.cluster.test

import hydra.cluster.eventlistener.SimpleClusterApp

import scalaj.http.Http

/**
  * For Hydra
  * Created by whereby[Tao Zhou](187225577@qq.com) on 2017/12/25
  */
object WebServerTest {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      SimpleClusterApp.startup(Seq("2551","0","0"))
      Thread.sleep(1000)
      Http("http://localhost:9000/app").postData(TestCons.pythonTestConfig).header("content-type", "application/json").asString
      println("App started")
    }
    else
      SimpleClusterApp.startup(args)
  }
}
