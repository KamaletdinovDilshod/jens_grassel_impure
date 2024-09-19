package impure.usecases

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.util.ByteString
import impure.models.TypeGenerators._
import impure.models._
import io.circe.syntax._

class SaveProduct extends BaseUseCaseSpec {
  final private val http = Http()

  override def beforeEach(): Unit = {
    flyway.clean()
    val _ = flyway.migrate()
  }

  override protected def afterEach(): Unit =
    flyway.clean()

  "Saving a Product" when {
    "the posted JSON is invalid" must {
      val expectedStatus = StatusCodes.BadRequest

      s"return $expectedStatus" in {
        for {
          resp <- http.singleRequest(
                    HttpRequest(
                      method = HttpMethods.POST,
                      uri = s"$baseUrl/products",
                      headers = Seq(),
                      entity = HttpEntity(
                        contentType = ContentTypes.`application/json`,
                        data = ByteString(
                          scala.util.Random.alphanumeric.take(256).mkString
                        )
                      )
                    )
                  )
        } yield resp.status must be(expectedStatus)
      }
    }
    "the posted JSON is valid" when {
      "the product does exist" must {
        val expectedStatus = StatusCodes.InternalServerError

        s"return $expectedStatus and not save the Product" in {
          (genProduct.sample, genProduct.sample) match {
            case (Some(a), Some(b)) =>
              val p = b.copy(id = a.id)
              for {
                _     <- repo.saveProduct(a)
                rows  <- repo.loadProduct(a.id)
                resp  <- http.singleRequest(
                           HttpRequest(
                             method = HttpMethods.POST,
                             uri = s"$baseUrl/products",
                             headers = Seq(),
                             entity = HttpEntity(
                               contentType = ContentTypes.`application/json`,
                               data = ByteString(p.asJson.noSpaces)
                             )
                           )
                         )
                rows2 <- repo.loadProduct(a.id)
              } yield {
                withClue("Sending product data failed!")(rows must not be empty)
                resp.status must be(expectedStatus)
                Product.fromDatabase(rows2) match {
                  case None    => fail("Sending product was not saved to database!")
                  case Some(s) => withClue("Existing product must not be changed!")(s mustEqual a)
                }
              }
            case _                  => fail("Could not generate data sample!")
          }
        }
      }
      "The product data does not exist" must {
        val expectedStatus = StatusCodes.OK

        s"return $expectedStatus and save the Product" in {
          genProduct.sample match {
            case None    => fail("Could not generate data sample!")
            case Some(p) =>
              for {
                resp <- http.singleRequest(
                          HttpRequest(
                            method = HttpMethods.POST,
                            uri = s"$baseUrl/products",
                            entity = HttpEntity(
                              contentType = ContentTypes.`application/json`,
                              data = ByteString(p.asJson.noSpaces)
                            )
                          )
                        )
                rows <- repo.loadProduct(p.id)
              } yield {
                resp.status must be(expectedStatus)
                Product.fromDatabase(rows) match {
                  case None    => fail("Product was not saved to database!")
                  case Some(s) => s mustEqual p
                }
              }
          }
        }
      }
    }
  }
}
