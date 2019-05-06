package com.example.repository

import com.example.Domain.{ User, Tag }
import org.mongodb.scala._
import org.bson.types.ObjectId
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{ fromProviders, fromRegistries }
import org.bson.codecs.configuration.CodecRegistry

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }

case class MongoUserRepository(login: String, password: String) extends UserRepository {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  val codecRegistry: CodecRegistry = fromRegistries(fromProviders(classOf[Tag], classOf[User]), DEFAULT_CODEC_REGISTRY)
  val mongoClient: MongoClient = MongoClient(s"mongodb://$login:$password@localhost")
  val database: MongoDatabase = mongoClient.getDatabase("db-users").withCodecRegistry(codecRegistry)
  val collection: MongoCollection[User] = database.getCollection("users")

  override def users(): Future[Seq[User]] = collection.find().toFuture()

  override def create(user: User): Future[ObjectId] = {

    val newUser = if (user._id.isEmpty) user.copy(_id = Some(new ObjectId())) else user

    collection.insertOne(newUser).subscribe(new Observer[Completed] {
      override def onNext(result: Completed): Unit = println(s"onNext: $result")
      override def onError(e: Throwable): Unit = println(s"onError: $e")
      override def onComplete(): Unit = println("onComplete")
    })
    Future { newUser._id.get }
  }
}

