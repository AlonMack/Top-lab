package co.toplab

import co.toplab.model.User
import org.bson.codecs.DecoderContext
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.bson.{BsonDocumentReader, BsonDocumentWrapper}
import org.mongodb.scala.bson.codecs.{DEFAULT_CODEC_REGISTRY, Macros}
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.{Completed, Document, MongoClient, MongoCollection, MongoDatabase, Observable, Observer}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.reflect.classTag

object UserDao {

  object UserCollection {
    val client: MongoClient = MongoClient()
    val database: MongoDatabase = client.getDatabase("mydb")
    val collection: MongoCollection[Document] = database.getCollection("mycoll")
  }


  def save(user: User): Long = {
    val id = Await.result(UserCollection.collection.countDocuments().toFuture(), 10.second)
    val username = user.username
    val email = user.email
    val age = user.age
    val document: Document = Document(
      "id" -> id,
      "username" -> username,
      "email" -> email,
      "age" -> age
    )
    val insertObservable: Observable[Completed] = UserCollection.collection.insertOne(document)

    insertObservable.subscribe(new Observer[Completed] {
      override def onNext(result: Completed): Unit = println(s"onNext: $result")

      override def onError(e: Throwable): Unit = println(s"onError: $e")

      override def onComplete(): Unit = println("onComplete")
    })
    id
  }

  def find(id: Long): Option[User] = {
    val eventualDocuments = UserCollection.collection.find(Filters.eq("id", id)).toFuture()
    val document = Await.result(eventualDocuments, 10.second).head
    val user: User = convertToUser(document)
    Option(user)
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

  def findAll(sort: Option[String]): List[User] =  {
  def from(sort: Option[String]) = sort match {
  case Some(v) if v.equalsIgnoreCase("desc")  => descending("id")
  case _                                      => ascending("id")
  }
    val result = UserCollection.collection.find()
      .sort(from(sort))
      .toFuture()
    val reports = Await.result(result, 10.second)
    reports.map(convertToUser).toList
  }

  def remove(id: Long): Unit = {
      Await.result(
        UserCollection.collection
          .deleteOne(Filters.eq("id", id))
          .toFuture,
        10.second)
  }

  def editAge(id: Long, age: Int): Unit = {
    Await.result(
      UserCollection.collection
        .updateOne(Filters.eq("id", id), set("age", age))
        .toFuture,
      10.second)
  }

}
