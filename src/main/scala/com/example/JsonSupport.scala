package com.example

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.bson.types.ObjectId
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat }

trait JsonSupport extends SprayJsonSupport {

  import DefaultJsonProtocol._

  implicit object ObjectIdJsonFormat extends JsonFormat[ObjectId] {
    def write(obj: ObjectId): JsValue = JsString(obj.toString)

    def read(json: JsValue): ObjectId = json match {
      case JsString(str) => new ObjectId(str)
      case _ => throw new DeserializationException(" string expected")
    }
  }

  implicit val tagJsonFormat = jsonFormat1(Domain.Tag)
  implicit val userJsonFormat = jsonFormat4(Domain.User)
  implicit val userCreatedJsonFormat = jsonFormat1(Dto.Id)
}
