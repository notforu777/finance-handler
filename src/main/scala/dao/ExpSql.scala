package dao

import cats.syntax.applicative._
import cats.syntax.either._
import domain.Errors.{ExpAlreadyExists, ExpNotFound}
import domain.{ExpId, ExpDescr, ExpAm, Expense}
import doobie.{ConnectionIO, Query0, Update0}
import doobie.implicits.toSqlInterpolator
import _root_.domain.ExpDescr

trait ExpSql {
  def createExpense(id: ExpId, description: ExpDescr, amount: ExpAm): ConnectionIO[Either[ExpAlreadyExists, Expense]]

  def getExpense(id: ExpId): ConnectionIO[Option[Expense]]

  def updateExpense(id: ExpId, description: ExpDescr, amount: ExpAm): ConnectionIO[Int]

  def deleteExpense(id: ExpId): ConnectionIO[Either[ExpNotFound, Unit]]

}

object ExpSql {
  object sqls {
    def insertSql(id: ExpId, description: ExpDescr, amount: ExpAm): Update0 =
      sql"""INSERT INTO expenses (description, amount) VALUES (${description.value}, ${amount.value}})""".update

    def getSql(id: ExpId): Query0[Expense] =
      sql"SELECT id, description, amount FROM expenses WHERE id=${id.value}".query[Expense]

    def updateSql(id: ExpId, description: ExpDescr, amount: ExpAm): Update0 =
      sql"UPDATE expenses SET description = ${description.value}, amount = ${amount.value} WHERE id=${id.value}".update

    def deleteSql(id: ExpId): Update0 =
      sql"DELETE FROM expenses WHERE id=${id.value}".update

  }

  private final class Impl extends ExpSql {

    import sqls._
    override def createExpense(id: ExpId, description: ExpDescr, amount: ExpAm): ConnectionIO[Either[ExpAlreadyExists, Expense]] =
      getSql(id).option.flatMap {
        case Some(_) => ExpAlreadyExists().asLeft[Expense].pure[ConnectionIO]
        case None =>
          insertSql(id, description, amount)
            .withUniqueGeneratedKeys[ExpId]("id")
            .map((id: ExpId) =>
              Expense(id, description, amount).asRight
            )
      }

    override def getExpense(id: ExpId): ConnectionIO[Option[Expense]] = getSql(id).option

    override def updateExpense(id: ExpId, descr: ExpDescr, amount: ExpAm): ConnectionIO[Int] = updateSql(id, descr, amount).run.map {
      case 0 => 0
      case _ => 1
    }

    override def deleteExpense(id: ExpId): ConnectionIO[Either[ExpNotFound, Unit]] = deleteSql(id).run.map {
      case 0 => ExpNotFound(id).asLeft
      case _ => ().asRight
    }
  }

  def make: ExpSql = new Impl
}


