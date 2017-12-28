package hydra.cluster.test

/**
  * For hydra.cluster.test in Hydra
  * Created by whereby[Tao Zhou](187225577@qq.com) on 2017/12/27
  */
object TestCons {
  val pythonTestConfig =  """
            |{
            | "appname": "appTest",
            | "startcmd":["python demo/app.py"],
            | "healthcheck":"http://localhost:5000/health",
            | "prestartcmd":[]
            |}
            """.stripMargin
}
