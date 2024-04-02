import cats.effect.{ExitCode, IO, IOApp, Resource}
import config.Config
import dao.ExpSql
import doobie.util.transactor.Transactor
import com.comcast.ip4s._
import controller.ExpController
import domain.RequestContext.ContextualIO
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import service.ExpStorage
import sttp.tapir.server.http4s.Http4sServerInterpreter
import tofu.logging.Logging

object Application extends IOApp {

  private val logger =
    Logging.Make.plain[IO].forService[Application.type]

  override def run(args: List[String]): IO[ExitCode] =
    (for {
      _ <- Resource.eval(logger.info("Starting todos service...."))

      config <- Resource.eval(Config.load)
      transactor = Transactor.fromDriverManager[ContextualIO](
        config.db.driver,
        config.db.url,
        config.db.user,
        config.db.password
      )

      sql = ExpSql.make
      storage: ExpStorage[ContextualIO] = ExpStorage.make[ContextualIO](sql, transactor)
      controller: ExpController[IO] = ExpController.make(storage)

      routes = Http4sServerInterpreter[IO]().toRoutes(controller.allEndpoints)
      httpApp = Router("/" -> routes).orNotFound
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(
          Ipv4Address.fromString(config.server.host).getOrElse(ipv4"0.0.0.0")
        )
        .withPort(Port.fromInt(config.server.port).getOrElse(port"80"))
        .withHttpApp(httpApp)
        .build
    } yield ())
      .useForever
      .as(ExitCode.Success)

}
