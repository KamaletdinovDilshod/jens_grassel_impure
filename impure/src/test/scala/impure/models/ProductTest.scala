package impure.models

import impure.models.TypeGenerators._
import cats.implicits._
import io.circe.parser._
import io.circe.syntax._

class ProductTest extends BaseSpec {
  "Product" when {
    "decoding from JSON" when {
      "JSON format is invalid" must {
        "return an error" in {
          forAll("input") { s: String =>
            decode[Product](s).isLeft must be(true)
          }
        }
      }

      "JSON format is valid" when {
        "data is invalid" must {
          "return an error" in {
            forAll("id", "names") { (id: String, ns: List[String]) =>
              val json = """{"id":""" + id.asJson.noSpaces + ""","names":""" + ns.asJson.noSpaces + """}"""
              decode[Product](json).isLeft must be(true)
            }
          }
        }

        "data is valid" must {
          "return the correct types" in {
            forAll("input") { i: Product =>
              val json = i.asJson.noSpaces
              withClue(s"Unable to decode JSON: $json") {
                decode[Product](json) match {
                  case Left(e)  => fail(e.getMessage)
                  case Right(v) => v must be(i)
                }
              }
            }
          }
        }
      }
    }
    "encoding to JSON" must {
      "return correct JSON" in {
        forAll("input") { i: Product =>
          val json = i.asJson.noSpaces
          json must include(s""""id":${i.id.asJson.noSpaces}""")
          json must include(s""""names":${i.names.asJson.noSpaces}""")
        }
      }
      "return decodeable JSON" in {
        forAll("input") { p: Product =>
          decode[Product](p.asJson.noSpaces) match {
            case Left(_)  => fail("Must be able to decode encoded JSON!")
            case Right(d) => withClue("Must decode the same product!")(d must be(p))
          }
        }
      }
    }
  }
}
