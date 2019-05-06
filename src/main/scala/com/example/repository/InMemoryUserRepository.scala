package com.example.repository

import akka.util.Timeout
import com.example.Domain.User
import org.bson.types.ObjectId

import scala.concurrent.duration._

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }

case class InMemoryUserRepository() extends UserRepository {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit lazy val timeout = Timeout(1.seconds)

  var usersMap: Map[ObjectId, User] = Map()

  override def users(): Future[Seq[User]] = Future { usersMap.values.toSeq }

  override def create(user: User): Future[ObjectId] = {
    val newUser = if (user._id.isEmpty) user.copy(_id = Some(new ObjectId())) else user

    usersMap += (newUser._id.get -> newUser)
    Future { newUser._id.get }
  }
}

