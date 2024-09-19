package impure.models

import io.circe._
import eu.timepit.refined.api._
import impure.models.TypeGenerators.arbitraryTranslation
import io.circe.parser._
import io.circe.refined._
import io.circe.syntax._

class TranslationTest extends BaseSpec {
  "Translation" when {
    "decoding from JSON" when {
      "JSON format is invalid" must {
        "return an error" in {
          forAll("input") { s: String =>
            decode[Translation](s).isLeft must be(true)
          }
        }
      }
      "JSON format is valid" when {
        "data is invalid" must {
          "return an error" in {
            forAll("lang", "name") { (l: String, n: String) =>
              whenever(
                RefType
                  .applyRef[LanguageCode](l)
                  .toOption
                  .isEmpty || RefType.applyRef[ProductName](n).toOption.isEmpty
              ) {
                val json = """{"lang":""" + l.asJson.noSpaces + ""","name": """ + n.asJson.noSpaces
                decode[Translation](json).isLeft must be(true)
              }
            }
          }
        }

        "data is valid" must {
          "return correct types" in {
            forAll("input") { i: Translation =>
              val json = i.asJson
              withClue(s"Unable to decode JSON: $json") {
                decode[Translation](json.noSpaces) match {
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
        forAll("input") { i: Translation =>
          val json = i.asJson.noSpaces
          json must include(s""""lang":${i.lang.asJson.noSpaces}""")
          json must include(s""""name":${i.name.asJson.noSpaces}""")
        }
      }
      "return decodeable JSON" in {
        forAll("input") { i: Translation =>
          decode[Translation](i.asJson.noSpaces) match {
            case Left(_)  => fail("Must be able to decode encoded JSON!")
            case Right(d) => withClue("Must decode the same prodct!")(d must be(i))
          }
        }
      }
    }

    "#fromUnsafe" when {
      "input is invalid" must {
        "return an empty Option" in {
          forAll("lang", "name") { (l: String, n: String) =>
            whenever(
              RefType
                .applyRef[LanguageCode](l)
                .toOption
                .isEmpty || RefType.applyRef[ProductName](n).toOption.isEmpty
            ) {
              Translation.fromUnsafe(l)(n) must be(empty)
            }
          }
        }
      }

      "input is valid" must {
        "return an Option to a Translation" in {
          forAll("input") { t: Translation =>
            Translation.fromUnsafe(t.lang.value)(t.name.value) must contain(t)
          }
        }
      }
    }
  }
}
