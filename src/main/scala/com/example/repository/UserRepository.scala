package com.example.repository

import scala.concurrent.Future
import com.example.Domain.User
import org.bson.types.ObjectId

trait UserRepository {
  def users(): Future[Seq[User]]
  def create(user: User): Future[ObjectId]
}
