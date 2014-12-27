package models
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._

case class Message(
  _id: BSONObjectID,
  subject: String,
  sender: String,
  participants: Seq[BSONObjectID],
  messageText: String
)

object Message {
  implicit val MessageFormat = Json.format[Message]
}
