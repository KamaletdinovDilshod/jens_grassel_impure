package impure.models

import java.util.UUID
import cats._
import cats.data._
import cats.implicits._
import eu.timepit.refined.auto._
import io.circe._

import scala.collection.Seq

final case class Product(id: ProductId, names: NonEmptySet[Translation])

object Product {

  implicit val decode: Decoder[Product] = Decoder.forProduct2("id", "names")(Product.apply)

  implicit val encode: Encoder[Product] = Encoder.forProduct2("id", "names")(p => (p.id, p.names))


  def fromDatabase(rows: Seq[(UUID, String, String)]): Option[Product] = {
    val po = for {
      (id, c, n) <- rows.headOption
      t <- Translation.fromUnsafe(c)(n)
      p <- Product(id = id, names = NonEmptySet.one[Translation](t)).some
    } yield p
    po.map(
      p =>
        rows.drop(1).foldLeft(p) { (a, cols) =>
          val (_, c, n) = cols
          Translation.fromUnsafe(c)(n).fold(a)(t => a.copy(names = a.names.add(t)))
        }
    )
  }

  implicit val order: Order[Product] = new Order[Product] {
    def compare(x: Product, y: Product): Int = x.id.compare(y.id)
  }
}
