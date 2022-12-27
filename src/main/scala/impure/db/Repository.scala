package impure.db

import java.util.UUID
import cats.data._
import eu.timepit.refined.auto._
import slick.basic._
import slick.jdbc._
import impure.models._

import scala.collection.Seq
import scala.concurrent.Future

final class Repository(val dbConfig: DatabaseConfig[JdbcProfile]) {

  import dbConfig.profile.api._

  final class Products(tag: Tag) extends Table[(UUID)](tag, "products") {
    def id = column[UUID]("id", O.PrimaryKey)

    def * = (id)
  }

  val productsTable = TableQuery[Products]

  final class Names(tag: Tag) extends Table[(UUID, String, String)](tag, "names") {
    def productId = column[UUID]("product_id")

    def langCode = column[String]("lang_code")

    def name = column[String]("name")

    def pk = primaryKey("names_pk", (productId, langCode))

    def productFk =
      foreignKey("names_product_id_fk", productId, productsTable)(
        _.id,
        onDelete = ForeignKeyAction.Cascade,
        onUpdate = ForeignKeyAction.Cascade
      )

    def * = (productId, langCode, name)
  }

  val namesTable = TableQuery[Names]

  def close(): Unit = dbConfig.db.close

  def loadProduct(id: ProductId): Future[Seq[(UUID, String, String)]] = {
    val program = for {
      (p, ns) <- productsTable
        .filter(_.id === id)
        .join(namesTable)
        .on(_.id === _.productId)
    } yield (p.id, ns.langCode, ns.name)
    dbConfig.db.run(program.result)
  }

  def loadProducts(): DatabasePublisher[(UUID, String, String)] = {
    val program = for {
      (p, ns) <- productsTable.join(namesTable).on(_.id === _.productId).sortBy(_._1.id)
    } yield (p.id, ns.langCode, ns.name)
    dbConfig.db.stream(program.result)
  }

  def saveProduct(p: Product): Future[List[Int]] = {
    val cp = productsTable += (p.id)
    val program = DBIO.sequence(cp :: saveTranslations(p).toList).transactionally
    dbConfig.db.run(program)
  }

  def updateProduct(p: Product): Future[List[Int]] = {
    val program = namesTable
      .filter(_.productId === p.id)
      .delete
      .andThen(DBIO.sequence(saveTranslations(p).toList))
      .transactionally
    dbConfig.db.run(program)
  }

  protected def saveTranslations(p: Product): NonEmptyList[DBIO[Int]] = {
    val save = saveTranslation(p.id)(_)
    p.names.toNonEmptyList.map(t => save(t))
  }

  protected def saveTranslation(id: ProductId)(t: Translation): DBIO[Int] =
    namesTable.insertOrUpdate((id, t.lang, t.name))


}
