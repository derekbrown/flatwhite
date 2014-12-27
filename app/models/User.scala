package models
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._

case class User(
  _id: BSONObjectID,
  firstName: String,
  lastName: String,
  userName: String,
  email: String
)

object User {
  implicit val UserFormat = Json.format[User]
}