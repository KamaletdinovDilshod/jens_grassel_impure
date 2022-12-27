package impure.models

import cats._
import cats.syntax.order._
import io.circe._
import io.circe.refined._

final case class Translation(lang: LanguageCode, name: ProductName)

object Translation {

  def fromUnsafe(lang: String)(name: String): Option[Translation] =
    for {
      l <- LanguageCode.from(lang).toOption
      n <- ProductName.from(name).toOption
    } yield Translation(lang = l, name = n)

  implicit val decode: Decoder[Translation] =
    Decoder.forProduct2("lang", "name")(Translation.apply)

  implicit val encode: Encoder[Translation] =
    Encoder.forProduct2("lang", "name")(t => (t.lang, t.name))

  implicit val order: Order[Translation] = new Order[Translation] {
    def compare(x: Translation, y: Translation): Int =
      x.lang.compare(y.lang)
  }
}
