package impure_test.db

import impure.models._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import cats.implicits._
import impure.models.TypeGenerators._
import impure_test.BaseSpec
import impure.db._
import java.util.UUID
import impure.Impure._

class RepositoryTest extends BaseSpec{
  private val dbConfig: DatabaseConfig[JdbcProfile] =
    DatabaseConfig.forConfig("database", system.settings.config)

  private val repo = new Repository(dbConfig)

  override protected def beforeEach(): Unit = {
    flyway.clean()
    val _ = flyway.migrate()
    super.beforeEach()
  }

  override protected def afterEach(): Unit = {
    flyway.clean()
    super.afterEach()
  }

  override protected def afterAll(): Unit = {
    repo.close()
    super.afterAll()
  }

  "#loadProduct" when {
    "the ID does not exist" must {
      "return an empty list of rows" in {
        val id = UUID.randomUUID
        for {
          rows <- repo.loadProduct(id)
        } yield {
          rows must be(empty)
        }
      }
    }
    "the ID exists" must {
      "return a list with all product rows" in {
        genProduct.sample match {
          case None => fail("Could generate data sample!")
          case Some(p) =>
            for {
              _ <- repo.saveProduct(p)
              rows <- repo.loadProduct(p.id)
            } yield {
              Product.fromDatabase(rows) match {
                case None => fail("No product created from database rows!")
                case Some(c) =>
                  c.id must be(p.id)
                  c mustEqual p
              }
            }
          }
        }
      }
  }
  "#saveProduct" when {
    "the product does not already exist" must {
      "save the product to the database" in {
        genProduct.sample match {
          case None => fail("Could not generate data sample!")
          case Some(p) =>
            for {
              cnts <- repo.saveProduct(p)
              rows <- repo.loadProduct(p.id)
            } yield {
              withClue("Data missing from database!")(
                cnts.fold(0)(_ + _) must be (p.names.toNonEmptyList.size + 1))
              Product.fromDatabase(rows) match {
                case None => fail("No product created from database rows!")
                case Some(c) =>
                  c.id must be(p.id)
                  c mustEqual p
              }
            }
        }
      }
    }
    "the product does already exist" must {
      "return an error and nt change the database" in {
        (genProduct.sample, genProduct.sample) match {
          case (Some(a), Some(b)) =>
            val p = b.copy(id = a.id)
            for {
              cnts <- repo.saveProduct(a)
              nosv <- repo.saveProduct(p).recover {
                case _ => 0
              }
              rows <- repo.loadProduct(a.id)
            } yield {
              withClue("Saving a duplicate product must fail!")(nosv must be(0))
              Product.fromDatabase(rows) match {
                case None => fail("No product created from database rows!")
                case Some(c) =>
                  c.id must be(a.id)
                  c mustEqual a
              }
            }
          case _ => fail("Could not create data sample")
        }
      }
    }
  }
  "updateProduct" when {
    "the product does exist" in {
      (genProduct.sample, genProduct.sample) match {
        case (Some(a), Some(b)) =>
          val p = b.copy(id = a.id)
          for {
            cnts <- repo.saveProduct(a)
            upds <- repo.updateProduct(p)
            rows <- repo.loadProduct(a.id)
          } yield {
            withClue("Already existing product was not created!")(
              cnts.fold(0)(_ + _) must be (a.names.toNonEmptyList.size + 1)
            )
            Product.fromDatabase(rows) match {
              case None => fail("No product created from database rows!")
              case Some(c) =>
                c.id must be(a.id)
                c mustEqual p
            }
          }
        case _ => fail("Could not create data sample!")
      }
    }
  }
  "The product does not exist" must {
    "return an error not change the database" in {
      genProduct.sample match {
        case None => fail("Could not generate data sample!")
        case Some(p) =>
          for {
            nosv <- repo.updateProduct(p).recover {
              case _ => 0
            }
            rows <- repo.loadProduct(p.id)
          } yield {
            withClue("Updating a not existing product must fail!")(nosv must be(0))
            withClue("Product must not exist indatabase!")(rows must be(empty))
          }
      }
    }
  }
}
