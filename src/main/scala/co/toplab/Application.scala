package co.toplab

import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze.BlazeBuilder
import scalaz.concurrent.Task

object Application extends ServerApp {

  override def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(UserController.service)
      .start
  }

}
