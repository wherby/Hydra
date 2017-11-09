package hydra.cluster.Cons

import com.typesafe.config.{Config, ConfigFactory}

import java.io.File


/**
  * For hydra-cluster-scala
  * Created by TaoZhou(187225577@qq.com) on 09/11/2017. 
  */
object HydraConfig {
  def load(): Config = {
    val hydraConfigLocate: String = scala.util.Properties.envOrElse("hydraconfig", "./hydra.conf")
    ConfigFactory.parseFile(new File(hydraConfigLocate))
      .withFallback(ConfigFactory.load()).resolve()
  }
}
