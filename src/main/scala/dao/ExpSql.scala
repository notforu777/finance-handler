package dao

import cats.syntax.applicative._
import cats.syntax.either._
import domain.Errors.{ExpAlreadyExists, ExpNotFound}
import domain.{ExpId, Expense}
import doobie.{ConnectionIO, Query0, Update0}
import doobie.implicits.toSqlInterpolator

trait ExpSql {
  def createExpense(expense: Expense): ConnectionIO[Either[ExpAlreadyExists, Expense]]

  def getExpense(id: ExpId): ConnectionIO[Option[Expense]]

  def updateExpense(id: ExpId, expense: Expense): ConnectionIO[Int]

  def deleteExpense(id: ExpId): ConnectionIO[Int]

}

object ExpSql {
  object sqls {
    def insertSql(exp: Expense): Update0 =
      sql"INSERT INTO expenses (description, amount) VALUES (${exp.description}, ${exp.amount})".update

    def getSql(id: ExpId): Query0[Expense] =
      sql"SELECT id, description, amount FROM expenses WHERE id = $id".query[Expense]

    def updateSql(id: ExpId, exp: Expense): Update0 =
      sql"UPDATE expenses SET description = ${exp.description}, amount = ${exp.amount} WHERE id = $id".update

    def deleteSql(id: ExpId): Update0 =
      sql"DELETE FROM expenses WHERE id = $id".update

  }

  private final class Impl extends ExpSql {

    import sqls._
    override def createExpense(exp: Expense): ConnectionIO[Either[ExpAlreadyExists, Expense]] =
      getSql(exp.id).option.flatMap {
        case Some(_) => ExpAlreadyExists().asLeft[Expense].pure[ConnectionIO]
        case None =>
          insertSql(exp)
            .withUniqueGeneratedKeys[ExpId]("id")
            .map((id: ExpId) =>
              Expense(id, exp.description, exp.amount).asRight
            )
      }

    override def getExpense(id: ExpId): ConnectionIO[Option[Expense]] = getSql(id).option

    override def updateExpense(id: ExpId, exp: Expense): ConnectionIO[Int] = updateSql(id, exp).run.map {
      case 0 => 0
      case _ => 1
    }

    override def deleteExpense(id: ExpId): ConnectionIO[Int] = deleteSql(id).run.map {
      case 0 => 0
      case _ => 1
    }
  }

  def make: ExpSql = new Impl
}


