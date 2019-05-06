package com.example

import akka.actor.ActorSystem
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.model.headers.HttpOriginRange
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

import scala.concurrent.Future
import akka.util.Timeout
import com.example.Domain.User
import com.example.Dto.Id
import com.example.repository.UserRepository

import scala.util.{ Failure, Success }

trait UserRoutes extends JsonSupport {

  implicit def system: ActorSystem
  lazy val log = Logging(system, classOf[UserRoutes])
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit lazy val timeout = Timeout(5.seconds)

  // Must be injected
  def userRepository: UserRepository

  // Used for development mode : allow CORS
  val corsSettings: CorsSettings = CorsSettings.defaultSettings.withAllowedOrigins(HttpOriginRange.*)

  lazy val userRoutes: Route = cors(corsSettings) {
    pathPrefix("users") {
      pathEnd {
        post {
          entity(as[User]) { user =>

            val userCreated: Future[Id] = userRepository.create(user).map(objectId => Id(objectId))

            onComplete(userCreated) {
              case Success(objectId) => complete((StatusCodes.Created, objectId))
              case Failure(e) => complete(HttpResponse(StatusCodes.InternalServerError, entity = e.toString))
            }
          }
        } ~
          get {
            import spray.json.DefaultJsonProtocol._
            val users: Future[Seq[User]] = userRepository.users()
            rejectEmptyResponse {
              complete(users)
            }
          }
      }
    }
  }
}
