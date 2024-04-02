package service

import cats.effect.kernel.MonadCancelThrow
import dao.ExpSql
import domain.Errors.AppError
import domain.{ExpId, Expense}
import doobie.Transactor
import domain.Errors._
import doobie.implicits._
import cats.implicits.toFunctorOps
import cats.syntax.applicativeError._
import cats.syntax.either._

trait ExpStorage[F[_]]{
  def create(exp: Expense): F[Either[AppError, Expense]]
  def get(id: ExpId): F[Either[InternalError, Option[Expense]]]
  def update(id: ExpId, exp: Expense): F[Either[InternalError, Int]]
  def delete(id: ExpId): F[Either[AppError, Unit]]
}

object ExpStorage {
  def make[F[_] : MonadCancelThrow](expSql: ExpSql, transactor: Transactor[F]): ExpStorage[F] =

    new ExpStorage[F] {
      override def create(exp: Expense): F[Either[AppError, Expense]] = expSql
        .createExpense(exp)
        .transact(transactor)
        .attempt
        .map {
          case Left(th) => InternalError(th).asLeft
          case Right(Left(error)) => error.asLeft
          case Right(Right(exp)) => exp.asRight
        }

      override def get(id: ExpId): F[Either[InternalError, Option[Expense]]] = expSql
        .getExpense(id)
        .transact(transactor)
        .attempt
        .map(_.leftMap(InternalError.apply))


      override def update(id: ExpId, exp: Expense): F[Either[InternalError, Int]] = expSql
        .updateExpense(id, exp)
        .transact(transactor)
        .attempt
        .map(_.leftMap(InternalError.apply))

      override def delete(id: ExpId): F[Either[AppError, Unit]] = expSql
        .deleteExpense(id)
        .transact(transactor)
        .attempt
        .map {
          case Left(th) => InternalError(th).asLeft
          case Right(Left(error)) => error.asLeft
          case _ => ().asRight
        }
    }

  doobie.free.connection.WeakAsyncConnectionIO
}
