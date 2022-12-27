package impure_test.usecases

import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.util.ByteString
import impure.models.TypeGenerators._
import impure.models._
import io.circe.syntax._

import scala.collection.immutable._

class UpdateProduct extends BaseUseCaseSpec {
  private final val http = Http()

  override protected def beforeEach(): Unit = {
    flyway.clean()
    val _ = flyway.migrate()
    super.beforeEach()
  }

  override protected def afterEach(): Unit = {
    flyway.clean()
    super.afterEach()
  }

  "Updating a product" when {
    "the posted JSON is invalid" must {
      val expectedStatus = StatusCodes.BadRequest

      s"return $expectedStatus and update the product" in {
        genProduct.sample match {
          case None => fail("Could not generate data sample!")
          case Some(p) =>
            for {
              _    <- repo.saveProduct(p)
              rows <- repo.loadProduct(p.id)
              resp <- http.singleRequest(
                HttpRequest(
                  method = HttpMethods.PUT,
                  uri = s"$baseUrl/product/${p.id}",
                  headers = Seq(),
                  entity = HttpEntity(
                    contentType = ContentTypes.`application/json`,
                    data = ByteString(scala.util.Random.alphanumeric.take(256).mkString)
                  )
                )
              )
              rows2 <- repo.loadProduct(p.id)
            } yield {
              withClue("Seeding product data failed!")(rows must not be (empty))
              resp.status must be(expectedStatus)
              Product.fromDatabase(rows2) match {
                case None    => fail("Sending product was not save to database!")
                case Some(s) => withClue("Existing product must not be changed!")(s mustEqual p)
              }
            }
        }
      }
    }
    "the posted JSON is valid" when {
      "the product does exist" must {
        val expectedStatus = StatusCodes.OK

        s"return $expectedStatus and update the Product" in {
          (genProduct.sample, genProduct.sample) match {
            case (Some(a), Some(b)) =>
              val p = b.copy(id = a.id)
              for {
                _    <- repo.saveProduct(a)
                rows <- repo.loadProduct(a.id)
                resp <- http.singleRequest(
                  HttpRequest(
                    method = HttpMethods.PUT,
                    uri = s"$baseUrl/product/${p.id}",
                    headers = Seq(),
                    entity = HttpEntity(
                      contentType = ContentTypes.`application/json`,
                      data = ByteString(p.asJson.noSpaces)
                    )
                  )
                )
                rows2 <- repo.loadProduct(p.id)
              } yield {
                withClue("Seeding product data failed!")(rows must not be (empty))
                resp.status must be(expectedStatus)
                Product.fromDatabase(rows2) match {
                  case None    => fail("Seeding product was not saved to database!")
                  case Some(s) => s mustEqual p
                }
              }
            case _ => fail("Could not generate data sample!")
          }
        }
      }
      "the product does not exist" must {
        val expectedStatus = StatusCodes.InternalServerError

        s"return $expectedStatus" in {
          genProduct.sample match {
            case None => fail("Could not generate data sample!")
            case Some(p) =>
              for {
                resp <- http.singleRequest(
                  HttpRequest(
                    method = HttpMethods.PUT,
                    uri = s"$baseUrl/product/${p.id}",
                    headers = Seq(),
                    entity = HttpEntity(
                      contentType = ContentTypes.`application/json`,
                      data = ByteString(p.asJson.noSpaces)
                    )
                  )
                )
                rows <- repo.loadProduct(p.id)
              } yield {
                resp.status must be(expectedStatus)
                rows must be(empty)
              }
          }
        }
      }
    }
  }
}
