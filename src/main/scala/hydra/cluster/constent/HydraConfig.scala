package hydra.cluster.constent

import com.typesafe.config.{Config, ConfigFactory}

import java.io.File


/**
  * For hydra-cluster-scala
  * Created by TaoZhou(187225577@qq.com) on 09/11/2017. 
  */
object HydraConfig {
  var _hydraConfig: Option[Config] = None
  def load(): Config = {
    _hydraConfig match {
      case None =>
        val hydraConfigLocate: String = scala.util.Properties.envOrElse("hydraconfig", "./hydra.conf")
        _hydraConfig= Some(ConfigFactory.parseFile(new File(hydraConfigLocate))
          .withFallback(ConfigFactory.load()).resolve())
        _hydraConfig.get
      case _=>_hydraConfig.get
    }
  }
}
