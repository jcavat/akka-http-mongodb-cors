package com.example

import org.bson.types.ObjectId

object Dto {
  // Use for the json answer : { id: ObjectId }
  case class Id(id: ObjectId)
}
