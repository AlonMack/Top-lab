package co.toplab

import co.toplab.UserService.{DuplicatedUsernameException, UserNotFoundException}
import co.toplab.model.User;
import io.circe._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import scalaz.concurrent.Task

import scala.util.Random

object UserController {

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]) = jsonOf[A]

  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]) = jsonEncoderOf[A]

  object SortQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("sort")

  private val errorHandler: PartialFunction[Throwable, Task[Response]] = {
    case UserNotFoundException(id) => BadRequest(s"User with id: $id not found!")
    case DuplicatedUsernameException(username) => Conflict(s"Username $username already in use!")
  }

  val service = HttpService {
    case GET -> Root / "users" :? SortQueryParamMatcher(sort) =>
      Ok(UserService.findAll(sort))
    case req@POST -> Root / "users" =>
      req.decode[User] { userForm =>
        val user = User(Random.nextInt(1000), userForm.username, userForm.email, userForm.age)
        UserService.save(user).flatMap(_ => Created(s"User with id: ${user.id}")).handleWith(errorHandler)
      }
    case GET -> Root / "users" / LongVar(id) =>
      Ok(UserService.find(id)).handleWith(errorHandler)
    case DELETE -> Root / "users" / LongVar(id) =>
      UserService.remove(id).flatMap(_ => NoContent()).handleWith(errorHandler)
    case req@PUT -> Root / "users" / LongVar(id) =>
      req.decode[User] { ageForm =>
        UserService.edit(id, ageForm.age).flatMap(_ => Accepted()).handleWith(errorHandler)
      }
  }

}
