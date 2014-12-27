package models
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._

case class Message(
  _id: BSONObjectID = BSONObjectID.generate,
  subject: String,
  sender: String,
  participants: Seq[BSONObjectID],
  messageText: String,
  id: Option[String] = None
)

object Message {
  implicit val messageJSONReads = __.json.update((__ \ 'id).json.copyFrom((__ \ '_id \ '$oid).json.pick[JsString] )) andThen Json.reads[Message]
  implicit val messageJSONWrites = Json.writes[Message]
}
