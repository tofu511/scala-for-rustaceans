package apiserver

import cats.effect._
import doobie._
import doobie.hikari.HikariTransactor
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s._
import org.flywaydb.core.Flyway
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import apiserver.config.AppConfig
import apiserver.repository.DoobieUserRepository
import apiserver.service.UserServiceImpl
import apiserver.http.UserRoutes

/*
 * MAIN APPLICATION
 * 
 * Application startup orchestration:
 * 1. Load configuration
 * 2. Run database migrations
 * 3. Create database transactor
 * 4. Wire up all layers
 * 5. Start HTTP server
 *
 * RUST COMPARISON:
 * Similar to main.rs in Rust web applications:
 * - Load config
 * - Run migrations
 * - Create connection pool
 * - Initialize services
 * - Start server
 *
 * Example Rust equivalent (axum):
 *   #[tokio::main]
 *   async fn main() -> Result<()> {
 *     let config = Config::load()?;
 *     sqlx::migrate!().run(&pool).await?;
 *     let app_state = AppState::new(pool);
 *     let app = Router::new()
 *       .route("/users", get(list_users).post(create_user))
 *       .with_state(app_state);
 *     axum::serve(listener, app).await?;
 *     Ok(())
 *   }
 */

object Main extends IOApp.Simple {
  
  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  
  // Run Flyway migrations
  def runMigrations(config: AppConfig): IO[Unit] = {
    IO {
      val flyway = Flyway.configure()
        .dataSource(
          config.database.url,
          config.database.user,
          config.database.password
        )
        .locations("classpath:db/migration")
        .baselineOnMigrate(true)
        .load()
      
      val result = flyway.migrate()
      result
    }.flatMap { result =>
      logger.info(s"Migrations complete: ${result.migrationsExecuted} executed")
    }
  }
  
  // Create database transactor with connection pooling
  def createTransactor(config: AppConfig): Resource[IO, HikariTransactor[IO]] = {
    for {
      ec <- ExecutionContexts.fixedThreadPool[IO](config.database.poolSize)
      xa <- HikariTransactor.newHikariTransactor[IO](
        driverClassName = config.database.driver,
        url = config.database.url,
        user = config.database.user,
        pass = config.database.password,
        connectEC = ec
      )
    } yield xa
  }
  
  def run: IO[Unit] = {
    for {
      _ <- logger.info("Starting API Server...")
      
      // Load configuration
      config <- AppConfig.load[IO]
      _ <- logger.info(s"Configuration loaded: ${config.server.host}:${config.server.port}")
      
      // Run migrations
      _ <- logger.info("Running database migrations...")
      _ <- runMigrations(config)
      
      // Start server
      _ <- createTransactor(config).use { xa =>
        // Wire up layers
        val repository = new DoobieUserRepository
        val service = new UserServiceImpl(repository)
        val routes = new UserRoutes(service, xa)
        
        // Build and start HTTP server
        EmberServerBuilder
          .default[IO]
          .withHost(Host.fromString(config.server.host).getOrElse(ipv4"0.0.0.0"))
          .withPort(Port.fromInt(config.server.port).getOrElse(port"8080"))
          .withHttpApp(routes.routesWithMiddleware.orNotFound)
          .build
          .use { server =>
            logger.info(s"✅ Server started at ${server.address}") *>
            logger.info("API endpoints:") *>
            logger.info("  GET    /users       - List all users") *>
            logger.info("  GET    /users/:id   - Get user by ID") *>
            logger.info("  POST   /users       - Create user") *>
            logger.info("  PUT    /users/:id   - Update user") *>
            logger.info("  DELETE /users/:id   - Delete user") *>
            logger.info("  GET    /health      - Health check") *>
            logger.info("\nPress Ctrl+C to stop...") *>
            IO.never
          }
      }
    } yield ()
  }
}
