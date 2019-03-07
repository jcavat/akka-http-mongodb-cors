package com.example

import akka.actor.{ Actor, ActorLogging, Props }
import akka.pattern.pipe
import org.mongodb.scala._
import org.bson.types.ObjectId
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{ fromProviders, fromRegistries }
import org.bson.codecs.configuration.CodecRegistry

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor }

// Structures to persist:
final case class User(_id: Option[ObjectId], name: String, email: String, tags: List[Tag])
final case class Tag(name: String)

object MongoUsersRegistryActor {
  // Commands
  final case object GetUsers
  final case class CreateUser(user: User)

  // Event
  final case class UserCreated(id: ObjectId)

  def props(login: String, password: String): Props = Props(new MongoUsersRegistryActor(login, password))
}

class MongoUsersRegistryActor(login: String, password: String) extends Actor with ActorLogging {

  import MongoUsersRegistryActor._
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  val codecRegistry: CodecRegistry = fromRegistries(fromProviders(classOf[Tag], classOf[User]), DEFAULT_CODEC_REGISTRY)

  val mongoClient: MongoClient = MongoClient(s"mongodb://$login:$password@localhost")
  val database: MongoDatabase = mongoClient.getDatabase("db-users").withCodecRegistry(codecRegistry)
  val collection: MongoCollection[User] = database.getCollection("users")

  def receive: Receive = {
    case GetUsers =>
      collection.find().toFuture() pipeTo sender()

    case CreateUser(user) =>
      collection.insertOne(user).subscribe(new Observer[Completed] {
        override def onNext(result: Completed): Unit = println(s"onNext: $result")
        override def onError(e: Throwable): Unit = println(s"onError: $e")
        override def onComplete(): Unit = println("onComplete")
      })
      sender() ! UserCreated(user._id.get)
  }
}
