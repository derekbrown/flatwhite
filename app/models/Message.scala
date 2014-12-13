package models

case class Participant(
  firstname: String,
  lastname: String,
  username: String
)

case class Message(
  subject: String,
  sender: String,
  participants: Seq[Participant],
  message: String
)

object JsonFormats {
  import play.api.libs.json._
  import play.api.data._
  import play.api.data.Forms._
  import play.api.libs.functional.syntax._

  implicit val participantFormat = Json.format[Participant]
  implicit val messageFormat = Json.format[Message]

}