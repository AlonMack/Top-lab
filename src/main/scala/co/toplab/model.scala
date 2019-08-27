package co.toplab

object model {

  case class User(id: Long, username: String, email: String, age: Int)
  case class UserForm(username: String, email: String, age: Int)
  case class UserAge(age: Int)
}
