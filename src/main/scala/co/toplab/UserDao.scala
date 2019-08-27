package co.toplab

import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.{Completed, Document, MongoClient, MongoCollection, MongoDatabase, Observable, Observer}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object UserDao {

  object UserCollection {
    val client: MongoClient = MongoClient()
    val database: MongoDatabase = client.getDatabase("mydb")
    val collection: MongoCollection[Document] = database.getCollection("mycoll")
  }


  def save(username: String, email: String, age: Int): Document = {
    val id = Await.result(UserCollection.collection.countDocuments().toFuture(), 10.second)
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
    document
  }

  def findById(id: Long): Future[Option[Document]] = {
    UserCollection.collection.find(Filters.eq("id", id)).first().toFutureOption()
  }

  def findByUserName(username: String): Future[Option[Document]] = {
    UserCollection.collection.find(Filters.eq("username", username)).first().toFutureOption()
  }


  def findAll(sort: Option[String]): Future[Seq[Document]] = {
    def from(sort: Option[String]) = sort match {
      case Some(v) if v.equalsIgnoreCase("desc") => descending("id")
      case _ => ascending("id")
    }

    UserCollection.collection.find()
      .sort(from(sort))
      .toFuture()
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
