package com.example

import org.bson.types.ObjectId

object Domain {
  final case class Tag(name: String)
  final case class User(_id: Option[ObjectId], name: String, email: String, tags: List[Tag])
}
