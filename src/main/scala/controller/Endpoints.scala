package controller

import domain.{ExpId, Expense, RequestContext}
import domain.Errors._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{PublicEndpoint, endpoint, header, path}
import sttp.tapir._

object Endpoints {

  val create
  : PublicEndpoint[(RequestContext, Expense), AppError, Expense, Any] =
    endpoint.post
      .in("expenses")
      .in(header[RequestContext]("Req-Id"))
      .in(jsonBody[Expense])
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Expense])

  val get
  : PublicEndpoint[(ExpId, RequestContext), AppError, Option[Expense], Any] =
    endpoint.get
      .in("expenses" / path[ExpId])
      .in(header[RequestContext]("Req-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Option[Expense]])

  val update
  : PublicEndpoint[(ExpId, RequestContext, Expense), AppError, Int, Any] =
    endpoint.put
      .in("expenses" / path[ExpId])
      .in(header[RequestContext]("Req-Id"))
      .in(jsonBody[Expense])
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Int])

  val delete
  : PublicEndpoint[(ExpId, RequestContext), AppError, Unit, Any] = {
    endpoint.delete
      .in("expenses" / path[ExpId])
      .in(header[RequestContext]("Req-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Unit])
  }

}
