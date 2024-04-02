package config

import cats.effect.IO
import pureconfig.generic.semiauto._
import pureconfig.ConfigReader
import pureconfig.ConfigSource

final case class Config(db: DbConfig, server: ServerConfig)
object Config {
  implicit val reader: ConfigReader[Config] = deriveReader

  def load: IO[Config] =
    IO.delay(ConfigSource.default.loadOrThrow[Config])
}

final case class DbConfig(
    url: String,
    driver: String,
    user: String,
    password: String
)
object DbConfig {
  implicit val reader: ConfigReader[DbConfig] = deriveReader
}

final case class ServerConfig(host: String, port: Int)
object ServerConfig {
  implicit val reader: ConfigReader[ServerConfig] = deriveReader
}
