package co.toplab

import org.http4s.server.Server
import org.http4s.server.blaze.BlazeBuilder

object HttpApi extends ServerApp {

  override def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(UserHttpEndpoint.service)
      .start
  }

}
