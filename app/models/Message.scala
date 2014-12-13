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

  // implicit val participantReads: Reads[Participant] = (
  //   (JsPath \ "firstname").read[String] and
  //   (JsPath \ "lastname").read[String] and
  //   (JsPath \ "username").read[String]
  // )(Participant.apply _)

  // implicit val participantWrites: Writes[Participant] = (
  //   (JsPath \ "firstname").write[String] and
  //   (JsPath \ "lastname").write[String] and
  //   (JsPath \ "username").write[String]
  // )(unlift(Participant.unapply))

  // implicit val messageReads: Reads[Message] = (
  //   (JsPath \ "subject").read[String] and
  //   (JsPath \ "sender").read[String] and
  //   (JsPath \ "participants").read[Seq[Participant]] and
  //   (JsPath \ "message").read[String]
  // )(Message.apply _)

  // implicit val messageWrites: Writes[Message] = (
  //   (JsPath \ "subject").write[String] and
  //   (JsPath \ "sender").write[String] and
  //   (JsPath \ "participants").write[Seq[Participant]] and
  //   (JsPath \ "message").write[String]
  // )(unlift(Message.unapply))

  // implicit val participantFormat: Format[Participant] = Format(participantReads, participantWrites)
  implicit val participantFormat = Json.format[Participant]
  // implicit val messageFormat: Format[Message] = Format(messageReads, messageWrites)
  implicit val messageFormat = Json.format[Message]

}