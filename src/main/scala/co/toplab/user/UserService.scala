package co.toplab.user

import cats.Applicative
import org.bson.codecs.DecoderContext
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.bson.{BsonDocumentReader, BsonDocumentWrapper}
import org.mongodb.scala.Document
import org.mongodb.scala.bson.codecs.{DEFAULT_CODEC_REGISTRY, Macros}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.reflect.classTag

trait UserService[F[_]] {
  def save(username: String, email: String, age: Int): F[User]

  def findById(id: Long): F[User]

  def findAll(sort: Option[String]): F[List[User]]

  def remove(id: Long): F[Unit]

  def edit(id: Long, age: Int): F[Unit]
}

object UserService {

  case class UserNotFoundException(id: Long) extends Exception

  case class DuplicatedUsernameException(username: String) extends Exception

  implicit val executor: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  implicit def apply[F[_]](implicit ev: UserService[F]): UserService[F] = ev

  def impl[F[_] : Applicative]: UserService[F] =
    new UserService[F] {

      import cats.implicits._

      private def findByUserName(username: String): Option[Document] = {
        Await.result(UserDao.findByUserName(username), 10.seconds)
      }

      private def convertToUser(document: Document) = {
        val personCodecProvider = Macros.createCodecProvider[User]()
        val codecRegistry: CodecRegistry = fromRegistries(fromProviders(personCodecProvider), DEFAULT_CODEC_REGISTRY)
        val bsonDocument = BsonDocumentWrapper.asBsonDocument(document, DEFAULT_CODEC_REGISTRY)

        val bsonReader = new BsonDocumentReader(bsonDocument)
        val decoderContext = DecoderContext.builder.build
        val codec = codecRegistry.get(classTag[User].runtimeClass)
        codec.decode(bsonReader, decoderContext).asInstanceOf[User]
      }

      override def save(username: String, email: String, age: Int): F[User] = {
        val result = findByUserName(username)
        if (result.isDefined)
          throw DuplicatedUsernameException(username)
        else {
          convertToUser(UserDao.save(username, email, age)).pure[F]
        }
      }

      override def findById(id: Long): F[User] = {
        val result = Await.result(UserDao.findById(id), 10.seconds)
        if (result.isDefined)
          convertToUser(result.get).pure[F]
        else
          throw UserNotFoundException(id)
      }

      override def findAll(sort: Option[String]): F[List[User]] = {
        val users = UserDao.findAll(sort).map(r => {
          r.map(convertToUser).toList
        })
        Await.result(users, 10.second).pure[F]
      }

      override def remove(id: Long): F[Unit] = {
        val result = Await.result(UserDao.findById(id), 10.seconds)
        if (result.isDefined)
          UserDao.remove(id).pure[F]
        else
          throw UserNotFoundException(id)
      }

      override def edit(id: Long, age: Int): F[Unit] = {
        val result = Await.result(UserDao.findById(id), 10.seconds)
        if (result.isDefined)
          UserDao.editAge(id, age).pure[F]
        else
          throw UserNotFoundException(id)
      }
    }
}
