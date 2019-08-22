package co.toplab

import co.toplab.model.User
import scalaz.concurrent.Task

object UserService {

  case class UserNotFoundException(id: Long) extends Exception

  case class DuplicatedUsernameException(username: String) extends Exception

  def save(user: User): Task[Unit] = {
    Task.now(UserDao.save(user))
  }

  def find(id: Long): Task[User] = UserDao.find(id) match {
    case Some(user) => Task.now(user)
    case None => Task.fail(UserNotFoundException(id))
  }

  def findAll(sort: Option[String]): Task[List[User]] = Task.delay {
    UserDao.findAll(sort)
  }

  def remove(id: Long): Task[Unit] = UserDao.find(id) match {
    case Some(user) => Task.delay {
      UserDao.remove(id)
    }
    case None => Task.fail(UserNotFoundException(id))
  }

  def edit(id: Long, age: Int): Task[Unit] = UserDao.find(id) match {
    case Some(user) => Task.delay {
      UserDao.editAge(id, age)
    }
    case None => Task.fail(UserNotFoundException(id))
  }
}
