package com.example

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.model.headers.HttpOriginRange
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

import scala.concurrent.Future
import com.example.MongoUsersRegistryActor._
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.mongodb.scala.bson.ObjectId

import collection.JavaConverters._
import scala.util.{ Failure, Success }

trait UserRoutes extends JsonSupport {

  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[UserRoutes])

  def userRegistryActor: ActorRef

  implicit lazy val timeout = Timeout(5.seconds)

  val tokens: List[String] = ConfigFactory.load().getStringList("tokens").asScala.toList

  val corsSettings: CorsSettings = CorsSettings.defaultSettings.withAllowedOrigins(HttpOriginRange.*)

  def check(credentials: Credentials): Option[String] = credentials match {
    case p @ Credentials.Provided(token) if tokens.exists(t => p.verify(t)) => Some(token)
    case _ => None
  }

  lazy val userRoutes: Route = cors(corsSettings) {
    pathPrefix("users") {
      authenticateOAuth2(realm = "secure site", check) { token =>
        pathEnd {
          concat(
            post {
              entity(as[User]) { user =>
                val newUser = if (user._id.isEmpty) user.copy(_id = Some(new ObjectId())) else user

                val userCreated: Future[UserCreated] =
                  (userRegistryActor ? CreateUser(newUser)).mapTo[UserCreated]

                onComplete(userCreated) {
                  case Success(objectId) => complete((StatusCodes.Created, objectId))
                  case Failure(e) => complete(HttpResponse(StatusCodes.InternalServerError, entity = e.toString))
                }
              }
            },
            get {
              import spray.json.DefaultJsonProtocol._
              val users: Future[Seq[User]] =
                (userRegistryActor ? GetUsers).mapTo[Seq[User]]
              rejectEmptyResponse {
                complete(users)
              }
            })
        }
      }
    }
  }
}
