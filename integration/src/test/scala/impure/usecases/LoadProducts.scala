package impure.usecases

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.util.ByteString
import io.circe.parser._
import impure.models.TypeGenerators._
import scala.concurrent.Future
import cats.implicits._
import impure.models.Product
import impure.models.TypeGenerators._

import scala.collection.immutable._

class LoadProducts extends BaseUseCaseSpec {
  final private val http = Http()

  override protected def beforeEach(): Unit = {
    val _ = flyway.migrate()
  }

  override protected def afterEach(): Unit =
    flyway.clean()

  "Loading all products" when {
    "no products exist" must {
      val expectedStatus = StatusCodes.OK

      s"return $expectedStatus and an empty list" in {
        for {
          resp <- http.singleRequest(
                    HttpRequest(
                      method = HttpMethods.GET,
                      uri = s"$baseUrl/products",
                      headers = Seq(),
                      entity = HttpEntity(
                        contentType = ContentTypes.`application/json`,
                        data = ByteString("")
                      )
                    )
                  )
          body <- resp.entity.dataBytes.runFold(ByteString(""))(_ ++ _)
        } yield {
          resp.status must be(expectedStatus)
          decode[List[Product]](body.utf8String) match {
            case Left(e)  => fail(s"Could not decode response: $e")
            case Right(d) => d must be(empty)
          }
        }
      }
    }

    "products exist" must {
      val expectedStatus = StatusCodes.OK

      s"return $expectedStatus and a list with all products" in {
        genProducts.sample match {
          case None     => fail("Could not generate data sample!")
          case Some(ps) =>
            for {
              _    <- Future.sequence(ps.map(p => repo.saveProduct(p)))
              resp <- http.singleRequest(
                        HttpRequest(
                          method = HttpMethods.GET,
                          uri = s"$baseUrl/products",
                          headers = Seq(),
                          entity = HttpEntity(
                            contentType = ContentTypes.`application/json`,
                            data = ByteString("")
                          )
                        )
                      )
              body <- resp.entity.dataBytes.runFold(ByteString(""))(_ ++ _)

            } yield {
              resp.status must be(expectedStatus)
              decode[List[Product]](body.utf8String) match {
                case Left(e)  => fail(s"Could not decode response: $e")
                case Right(d) => ps.sorted mustEqual d.sorted
              }
            }
        }
      }
    }
  }
}
