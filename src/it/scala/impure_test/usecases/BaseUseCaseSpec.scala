package impure_test.usecases

/*
 * CC0 1.0 Universal (CC0 1.0) - Public Domain Dedication
 *
 *                                No Copyright
 *
 * The person who associated a work with this deed has dedicated the work to
 * the public domain by waiving all of his or her rights to the work worldwide
 * under copyright law, including all related and neighboring rights, to the
 * extent allowed by law.
 */


import java.net.ServerSocket
import akka.actor._
import akka.http.scaladsl._
import akka.http.scaladsl.server.Directives._
import akka.testkit.TestKit
import impure_test.usecases.BaseUseCaseActor.BaseUseCaseActorCmds
import com.typesafe.config._
import impure.api.{ProductRoutes, ProductsRoutes}
import impure.db.Repository
import org.flywaydb.core.Flyway
import org.scalatest._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import slick.basic._
import slick.jdbc._
import akka.stream.SystemMaterializer

import scala.concurrent.duration._

abstract class BaseUseCaseSpec
  extends TestKit(
    ActorSystem(
      "it-test",
      ConfigFactory
        .parseString(s"api.port=${BaseUseCaseSpec.findAvailablePort()}")
        .withFallback(ConfigFactory.load())
    )
  )
    with AsyncWordSpecLike
    with MustMatchers
    with ScalaCheckPropertyChecks
    with BeforeAndAfterAll
    with BeforeAndAfterEach {
  implicit val materializer: SystemMaterializer = SystemMaterializer(system)


  final val baseUrl: String = s"""http://${system.settings.config
    .getString("api.host")}:${system.settings.config
    .getInt("api.port")}"""

  private val url = "jdbc:postgresql://" +
    system.settings.config.getString("database.db.properties.serverName") +
    ":" + system.settings.config
    .getString("database.db.properties.portNumber") +
    "/" + system.settings.config
    .getString("database.db.properties.databaseName")
  private val user = system.settings.config.getString("database.db.properties.user")
  private val pass =
    system.settings.config.getString("database.db.properties.password")
  protected val flyway: Flyway = Flyway.configure().dataSource(url, user, pass).load()

  protected val dbConfig: DatabaseConfig[JdbcProfile] =
    DatabaseConfig.forConfig("database", system.settings.config)
  protected val repo = new Repository(dbConfig)

  override protected def afterAll(): Unit =
    TestKit.shutdownActorSystem(system, FiniteDuration(5, SECONDS))


  override protected def beforeAll(): Unit = {
    val _ = flyway.migrate()
    val a = system.actorOf(BaseUseCaseActor.props(repo, materializer))
    a ! BaseUseCaseActorCmds.Start
  }
}

object BaseUseCaseSpec {

  def findAvailablePort(): Int = {
    val serverSocket = new ServerSocket(0)
    val freePort     = serverSocket.getLocalPort
    serverSocket.setReuseAddress(true) // Allow instant rebinding of the socket.
    serverSocket.close()
    freePort
  }
}

final class BaseUseCaseActor(repo: Repository, mat: SystemMaterializer) extends Actor with ActorLogging {
  import context.dispatcher

  implicit val system: ActorSystem             = context.system
  implicit val materializer: SystemMaterializer = mat

  override def receive: Receive = {
    case BaseUseCaseActorCmds.Start =>
      val productRoutes  = new ProductRoutes(repo)
      val productsRoutes = new ProductsRoutes(repo)
      val routes         = productRoutes.routes ~ productsRoutes.routes
      val host           = context.system.settings.config.getString("api.host")
      val port           = context.system.settings.config.getInt("api.port")
      val _              = Http().bindAndHandle(routes, host, port)
    case BaseUseCaseActorCmds.Stop =>
      context.stop(self)
  }
}

object BaseUseCaseActor {

  def props(repo: Repository, mat: SystemMaterializer): Props = Props(new BaseUseCaseActor(repo, mat))


  sealed trait BaseUseCaseActorCmds

  object BaseUseCaseActorCmds {

    case object Start extends BaseUseCaseActorCmds

    case object Stop extends BaseUseCaseActorCmds
  }
}

