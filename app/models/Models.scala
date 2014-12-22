package models
import play.api.libs.json._
import reactivemongo.bson._
import play.modules.reactivemongo.json.BSONFormats._

case class User(
  id: String,
  firstName: String,
  lastName: String,
  userName: String,
  email: String
)

case class Message(
  id: String,
  subject: String,
  sender: String,
  participants: List[String],
  messageText: String
)

object JsonFormats {
  import play.api.libs.json._
  import play.api.data._
  import play.api.data.Forms._
  import play.api.libs.functional.syntax._

  implicit val userFormat = Json.format[User]
  implicit val messageFormat = Json.format[Message]

}

object MessageBSONReader extends BSONDocumentReader[Message] {
  def read(document: BSONDocument) :Message = {
    Message(
      document.getAs[BSONObjectID]("_id").map(_.stringify).getOrElse(""),
      document.getAs[String]("subject").getOrElse(""),
      document.getAs[String]("sender").getOrElse(""),
      document.getAs[List[String]]("participants").getOrElse(List()),
      document.getAs[String]("messageText").getOrElse("")
    )
  }
}

object MessageBSONWriter extends BSONDocumentWriter[Message] {
  def write(message: Message) :BSONDocument = {
    val bson = BSONDocument(
      "_id" -> BSONObjectID.generate,
      "subject" -> BSONString(message.subject),
      "sender" -> BSONString(message.sender),
      "participants" -> BSONArray(message.participants),
      "messageText" -> BSONString(message.messageText)
    )
    return bson
  }
}