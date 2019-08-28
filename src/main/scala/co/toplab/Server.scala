package co.toplab

import cats.effect._
import co.toplab.user._
import org.http4s.HttpRoutes
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{Server => BlazeServer}

object Server {

  def run[F[_] : ConcurrentEffect : ContextShift : Timer]: Resource[F, BlazeServer[F]] = {
    val userService = UserService.impl[F]
    val routes = UserRoutes[F](userService)
    for {
      svr <- server[F](routes)
    } yield svr
  }

  private[this] def server[F[_] : ConcurrentEffect : ContextShift : Timer](routes: HttpRoutes[F]
                                                                          ): Resource[F, BlazeServer[F]] = {
    import org.http4s.implicits._

    BlazeServerBuilder[F]
      .bindHttp(8080, "localhost")
      .withHttpApp(routes.orNotFound)
      .resource
  }

}
