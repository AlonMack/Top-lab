package co.toplab.user

import cats.effect.{ContextShift, Sync}
import co.toplab.user.UserService.{DuplicatedUsernameException, UserNotFoundException}
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import org.http4s.json4s.native.jsonOf
import org.http4s.{EntityDecoder, HttpRoutes, Response}
import org.json4s._
import org.json4s.native.Serialization.write

import scala.language.higherKinds
import scala.util.{Failure, Success, Try}
;

object UserRoutes {
  implicit val fmt: DefaultFormats.type = org.json4s.DefaultFormats

  case class UserResponse(username: String, email: String, age: Int)

  case class UserAgeResponse(age: Int)

  implicit val userResponseReader: Reader[UserResponse] = (value: JValue) => {
    value.extract[UserResponse]
  }
  implicit val userAgeResponseReader: Reader[UserAgeResponse] = (value: JValue) => {
    value.extract[UserAgeResponse]
  }

  object SortQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("sort")


  def apply[F[_] : Sync : ContextShift](userService: UserService[F]): HttpRoutes[F] = {
    object dsl extends Http4sDsl[F]
    import dsl._
    implicit val userDecoder: EntityDecoder[F, UserResponse] = jsonOf[F, UserResponse]
    implicit val userAgeDecoder: EntityDecoder[F, UserAgeResponse] = jsonOf[F, UserAgeResponse]

    val errorHandler: PartialFunction[Throwable, F[Response[F]]] = {
      case UserNotFoundException(id) =>
        BadRequest(s"User with id: $id not found!")
      case DuplicatedUsernameException(username) =>
        Conflict(s"Username $username already in use!")

    }
    HttpRoutes.of[F] {
      case GET -> Root / "users" :? SortQueryParamMatcher(sort) =>
        Ok(write(userService.findAll(sort)))
      case req@POST -> Root / "users" =>
        req.decode[UserResponse] { user =>
          val value = Try(userService.save(user.username, user.email, user.age))
          value match {
            case Success(u) => Created(write(u))
            case Failure(e) => errorHandler(e)
          }
        }
      case GET -> Root / "users" / LongVar(id) =>
        val value = Try(userService.findById(id))
        value match {
          case Success(u) => Ok(write(u))
          case Failure(e) => errorHandler(e)
        }
      case DELETE -> Root / "users" / LongVar(id) =>
        val value = Try(userService.remove(id))
        value match {
          case Success(_) => NoContent()
          case Failure(e) => errorHandler(e)
        }
      case req@PUT -> Root / "users" / LongVar(id) =>
        req.decode[UserAgeResponse] { userAge =>
          val value = Try(userService.edit(id, userAge.age))
          value match {
            case Success(_) => Accepted()
            case Failure(e) => errorHandler(e)
          }
        }
    }
  }
}
