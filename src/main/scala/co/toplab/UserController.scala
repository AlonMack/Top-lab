package co.toplab

import co.toplab.UserService.{DuplicatedUsernameException, UserNotFoundException}
import co.toplab.model.{UserAge, UserForm}
import org.http4s._
import org.http4s.dsl.{Conflict, _}
import org.http4s.json4s.native._
import org.json4s.native.Serialization.write
import org.json4s.{JValue, Reader, _}
import scalaz.concurrent.Task

object UserController {
  implicit val fmt: DefaultFormats.type = org.json4s.DefaultFormats

  implicit val userFormReader: Reader[UserForm] = new Reader[UserForm] {
    def read(value: JValue): UserForm = {
      value.extract[UserForm]
    }
  }
  implicit val userFormDec: EntityDecoder[UserForm] = jsonOf[UserForm]

  implicit val userAgeReader: Reader[UserAge] = new Reader[UserAge] {
    def read(value: JValue): UserAge = {
      value.extract[UserAge]
    }
  }
  implicit val userAgeDec: EntityDecoder[UserAge] = jsonOf[UserAge]

  object SortQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("sort")

  private val errorHandler: PartialFunction[Throwable, Task[Response]] = {
    case UserNotFoundException(id) => BadRequest(s"User with id: $id not found!")
    case DuplicatedUsernameException(username) => Conflict(s"Username $username already in use!")
  }

  val service = HttpService {
    case GET -> Root / "users" :? SortQueryParamMatcher(sort) =>
      UserService.findAll(sort).flatMap(users => Ok(write(users)))
    case req@POST -> Root / "users" =>
      req.decode[UserForm] { user =>
        val result = UserService.save(user.username, user.email, user.age)
        result.flatMap(user => Created(user.id.toString)).handleWith(errorHandler)
      }
    case GET -> Root / "users" / LongVar(id) =>
      UserService.findById(id).flatMap(users => Ok(write(users))).handleWith(errorHandler)
    case DELETE -> Root / "users" / LongVar(id) =>
      UserService.remove(id).flatMap(_ => NoContent()).handleWith(errorHandler)
    case req@PUT -> Root / "users" / LongVar(id) =>
      req.decode[UserAge] { userAge =>
        UserService.edit(id, userAge.age).flatMap(_ => Accepted()).handleWith(errorHandler)
      }
  }
}
