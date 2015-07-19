package models

import play.api.libs.json._

case class Cat(id: Long, name: String, color: String, breed: Int, gender: Int, filename: String)

object Cat {
  
  implicit val catFormat = Json.format[Cat]
}