package impure.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import eu.timepit.refined.auto._
import impure.db.Repository
import impure.models.Product
import impure.models._

import scala.concurrent.{ExecutionContext, Future}

final class ProductRoutes(repo: Repository)(implicit ec: ExecutionContext) {
  val routes: Route = path("product" / JavaUUID) { id: ProductId =>
    get {
      rejectEmptyResponse {
        complete {
          for {
            rows <- repo.loadProduct(id)
            prod <- Future {
              Product.fromDatabase(rows)
            }
          } yield prod
        }
      }
    } ~ put {
      entity(as[Product]) { p =>
        complete {
          repo.updateProduct(p)
        }
      }
    }
  }
}
