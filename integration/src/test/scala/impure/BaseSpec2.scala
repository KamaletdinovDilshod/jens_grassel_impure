package impure

import akka.actor._
import akka.stream._
import akka.testkit.TestKit
import com.typesafe.config._
import org.flywaydb.core.Flyway
import org.scalatest._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.net.ServerSocket
import scala.concurrent.duration.{FiniteDuration, _}

abstract class BaseSpec2
  extends TestKit(
    ActorSystem(
      "it-test",
      ConfigFactory
        .parseString(s"api.port=${BaseSpec2.findAvailablePort()}")
        .withFallback(ConfigFactory.load())
    )
  )
  with AsyncWordSpecLike
  with Matchers
  with ScalaCheckPropertyChecks
  with BeforeAndAfterAll
  with BeforeAndAfterEach {

  implicit val materializer: Materializer = Materializer(system)

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

  /**
   * Shutdown the actor system after the tests have run. If the system does not terminate within the given time frame an
   * error is thrown.
   */
  override protected def afterAll(): Unit =
    TestKit.shutdownActorSystem(system, FiniteDuration(5, SECONDS))

  /**
   * Initialise the database before any tests are run.
   */
  override protected def beforeAll(): Unit = {
    val _ = flyway.migrate()
  }
}

object BaseSpec2 {

  /**
   * Start a server socket and close it. The port number used by the socket is considered free and returned.
   *
   * @return
   *   A port number.
   */
  def findAvailablePort(): Int = {
    val serverSocket = new ServerSocket(0)
    val freePort     = serverSocket.getLocalPort
    serverSocket.setReuseAddress(true) // Allow instant rebinding of the socket.
    serverSocket.close()
    freePort
  }
}
