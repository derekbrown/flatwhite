package models
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._

case class User(
  _id: BSONObjectID = BSONObjectID.generate,
  firstName: String,
  lastName: String,
  userName: String,
  email: String,
  id: Option[String] = None
)

object User {
  implicit val userJSONReads = Json.reads[User]
  implicit val userJSONWrites = Json.writes[User]
}