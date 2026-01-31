package apiserver.config

import cats.effect._
import com.typesafe.config.ConfigFactory
import io.circe.config.parser

/*
 * CONFIGURATION LAYER
 * 
 * Load configuration from application.conf and environment variables.
 *
 * RUST COMPARISON:
 * Similar to config crates in Rust (e.g., config-rs, figment):
 * - Load from files and environment
 * - Type-safe configuration
 * - Layered configuration (file → env vars)
 */

case class DatabaseConfig(
  url: String,
  user: String,
  password: String,
  driver: String,
  poolSize: Int
)

case class ServerConfig(
  host: String,
  port: Int
)

case class AppConfig(
  database: DatabaseConfig,
  server: ServerConfig
)

object AppConfig {
  
  def load[F[_]: Sync]: F[AppConfig] = Sync[F].delay {
    val config = ConfigFactory.load()
    
    val dbConfig = DatabaseConfig(
      url = config.getString("database.url"),
      user = config.getString("database.user"),
      password = config.getString("database.password"),
      driver = config.getString("database.driver"),
      poolSize = config.getInt("database.pool-size")
    )
    
    val serverConfig = ServerConfig(
      host = config.getString("server.host"),
      port = config.getInt("server.port")
    )
    
    AppConfig(dbConfig, serverConfig)
  }
}
