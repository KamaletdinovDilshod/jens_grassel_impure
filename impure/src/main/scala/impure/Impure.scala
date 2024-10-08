package impure

import akka.http.scaladsl.server.Directives._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer, SystemMaterializer}
import impure.db.Repository
import org.flywaydb.core.Flyway
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import impure.api._

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object Impure {

  /**
   * Main entry point of the application.
   *
   * @param args
   *   A list of arguments given on the command line.
   */
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem  = ActorSystem()
    implicit val mat: Materializer    = Materializer(system)
    implicit val ec: ExecutionContext = system.dispatcher

    val url            = "jdbc:postgresql://" +
      system.settings.config.getString("database.db.properties.serverName") +
      ":" + system.settings.config.getString("database.db.properties.portNumber") +
      "/" + system.settings.config.getString("database.db.properties.databaseName")
    val user           = system.settings.config.getString("database.db.properties.user")
    val pass           = system.settings.config.getString("database.db.properties.password")
    val flyway: Flyway = Flyway.configure().dataSource(url, user, pass).load()
    val _              = flyway.migrate()

    val dbConfig: DatabaseConfig[JdbcProfile] =
      DatabaseConfig.forConfig("database", system.settings.config)
    val repo                                  = new Repository(dbConfig)

    val productRoutes  = new ProductRoutes(repo)
    val productsRoutes = new ProductsRoutes(repo)
    val routes         = productRoutes.routes ~ productsRoutes.routes

    val host       = system.settings.config.getString("api.host")
    val port       = system.settings.config.getInt("api.port")
    val srv        = Http().newServerAt(host, port).bindFlow(routes)
    srv
      .flatMap { binding => binding.whenTerminated // Wait for termination
      }
      .onComplete(_ => system.terminate)
  }
}
