package controller

import cats.data.ReaderT
import cats.effect.IO
import domain.{ExpId, RequestContext}
import service.ExpStorage
import sttp.tapir.server.ServerEndpoint

trait ExpController[F[_]] {
  def createEnd: ServerEndpoint[Any, F]

  def getEnd: ServerEndpoint[Any, F]

  def updateEnd(): ServerEndpoint[Any, F]

  def deleteEnd(): ServerEndpoint[Any, F]

  def allEndpoints: List[ServerEndpoint[Any, F]]
}

object ExpController {
  final private class Impl(storage: ExpStorage[ReaderT[IO, RequestContext, *]]) extends ExpController[IO] {
    override def createEnd: ServerEndpoint[Any, IO] = Endpoints.create.serverLogic {
      case (ctx, exp) =>
        storage.create(exp).run(ctx)
    }

    override def getEnd: ServerEndpoint[Any, IO] = Endpoints.get.serverLogic {
      case (expId: ExpId, ctx) =>
        storage.get(expId).run(ctx)
    }

    override def updateEnd: ServerEndpoint[Any, IO] = Endpoints.update.serverLogic {
      case (expId: ExpId, ctx, exp)  =>
        storage.update(expId, exp).run(ctx)
    }

    override def deleteEnd: ServerEndpoint[Any, IO] = Endpoints.delete.serverLogic {
      case (expId: ExpId, ctx) =>
        storage.delete(expId).run(ctx)
    }

    override def allEndpoints: List[ServerEndpoint[Any, IO]] = List(
      createEnd,
      getEnd,
      updateEnd,
      deleteEnd
    )
  }

  def make(storage: ExpStorage[ReaderT[IO, RequestContext, *]]): ExpController[IO] = new Impl(storage)
}
