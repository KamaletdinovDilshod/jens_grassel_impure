package impure_test

import java.net.ServerSocket
import akka.actor._
import akka.stream._
import akka.testkit.TestKit
import com.typesafe.config._
import org.flywaydb.core.Flyway
import org.scalatest._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import scala.concurrent.duration._

import scala.concurrent.duration.FiniteDuration

abstract class BaseSpec extends TestKit(
  ActorSystem(
    "it-test",
    ConfigFactory
      .parseString(s"api.port=${BaseSpec.findAvailablePort()}")
      .withFallback(ConfigFactory.load())
  )
)
  with AsyncWordSpecLike
  with MustMatchers
  with ScalaCheckPropertyChecks
  with BeforeAndAfterAll
  with BeforeAndAfterEach {

  implicit val materializer: SystemMaterializer = SystemMaterializer(system)

  private val url = "jdbc:postgresql://" +
    system.settings.config.getString("database.db.properties.serverName") +
    ":" + system.settings.config
    .getString("database.db.properties.portNumber") +
    "/" + system.settings.config
    .getString("database.db.properties.databaseName")

  private val user = system.settings.config.getString("database.db.properties.user")

  private val pass = system.settings.config.getString("database.db.properties.password")

  protected val flyway: Flyway = Flyway.configure().dataSource(url, user, pass).load()

  override protected def afterAll(): Unit =
    TestKit.shutdownActorSystem(system, FiniteDuration(5, SECONDS))

  override protected def beforeAll(): Unit = {
    val _ = flyway.baseline()
  }
}

object BaseSpec {

  def findAvailablePort(): Int = {

    val serverSocket = new ServerSocket(0)
    val freePort = serverSocket.getLocalPort
    serverSocket.setReuseAddress(true)
    serverSocket.close()
    freePort
  }
}
