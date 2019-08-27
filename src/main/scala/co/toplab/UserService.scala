package co.toplab

import co.toplab.model.User
import org.bson.codecs.DecoderContext
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.bson.{BsonDocumentReader, BsonDocumentWrapper}
import org.mongodb.scala.Document
import org.mongodb.scala.bson.codecs.{DEFAULT_CODEC_REGISTRY, Macros}
import scalaz.concurrent.Task

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration._
import scala.reflect.classTag

object UserService {
  implicit val executor: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  case class UserNotFoundException(id: Long) extends Exception

  case class DuplicatedUsernameException(username: String) extends Exception

  def save(username: String, email: String, age: Int): Task[User] = {
    val result = findByUserName(username)
    if (result.isDefined)
      Task.fail(DuplicatedUsernameException(username))
    else
      Task.delay(convertToUser(UserDao.save(username, email, age)))
  }

  def findById(id: Long): Task[User] = {
    val result = Await.result(UserDao.findById(id), 10.seconds)
    if (result.isDefined)
      Task.delay {
        convertToUser(result.get)
      }
    else
      Task.fail(UserNotFoundException(id))
  }

  private def findByUserName(username: String): Option[Document] = {
    Await.result(UserDao.findByUserName(username), 10.seconds)
  }

  def findAll(sort: Option[String]): Task[List[User]] = {
    val x = UserDao.findAll(sort).map(r => {
      r.map(convertToUser).toList
    })
    Task.delay {
      Await.result(x, 10.second)
    }
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


  def remove(id: Long): Task[Unit] = {
    val result = Await.result(UserDao.findById(id), 10.seconds)
    if (result.isDefined)
      Task.delay(UserDao.remove(id))
    else
      Task.fail(UserNotFoundException(id))
  }

  def edit(id: Long, age: Int): Task[Unit] = {
    val result = Await.result(UserDao.findById(id), 10.seconds)
    if (result.isDefined)
      Task.delay(UserDao.editAge(id, age))
    else
      Task.fail(UserNotFoundException(id))
  }
}
