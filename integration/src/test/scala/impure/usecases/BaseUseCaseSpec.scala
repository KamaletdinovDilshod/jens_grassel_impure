package impure.usecases

import akka.actor._
import akka.http.scaladsl._
import akka.http.scaladsl.server.Directives._
import akka.stream.{Materializer, SystemMaterializer}
import akka.testkit.TestKit
import com.typesafe.config._
import impure.api.{ProductRoutes, ProductsRoutes}
import impure.db.Repository
import org.flywaydb.core.Flyway
import org.scalatest._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import slick.basic._
import slick.jdbc._
import BaseUseCaseActor.BaseUseCaseActorCmds

import java.net.ServerSocket
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
  with Matchers
  with ScalaCheckPropertyChecks
  with BeforeAndAfterAll
  with BeforeAndAfterEach {

  implicit val materializer: SystemMaterializer = SystemMaterializer(system)

  final val baseUrl: String = s"""http://${system.settings.config
      .getString("api.host")}:${system.settings.config
      .getInt("api.port")}"""

  private val url              = "jdbc:postgresql://" +
    system.settings.config.getString("database.db.properties.serverName") +
    ":" + system.settings.config
      .getString("database.db.properties.portNumber") +
    "/" + system.settings.config
      .getString("database.db.properties.databaseName")
  private val user             = system.settings.config.getString("database.db.properties.user")
  private val pass             =
    system.settings.config.getString("database.db.properties.password")
  protected val flyway: Flyway = Flyway.configure().dataSource(url, user, pass).cleanDisabled(false).load()

  protected val dbConfig: DatabaseConfig[JdbcProfile] =
    DatabaseConfig.forConfig("database", system.settings.config)
  protected val repo                                  = new Repository(dbConfig)

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

  implicit val system: ActorSystem              = context.system
  implicit val materializer: SystemMaterializer = mat

  override def receive: Receive = {
    case BaseUseCaseActorCmds.Start =>
      val productRoutes  = new ProductRoutes(repo)
      val productsRoutes = new ProductsRoutes(repo)
      val routes         = productRoutes.routes ~ productsRoutes.routes
      val host           = context.system.settings.config.getString("api.host")
      val port           = context.system.settings.config.getInt("api.port")
      val _              = Http().newServerAt(host, port).bindFlow(routes)
    case BaseUseCaseActorCmds.Stop  =>
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
