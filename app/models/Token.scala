package models

import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import securesocial.core.providers.MailToken

case class Token(
  mailToken: MailToken
)

object Token {

  implicit val mtJSONReads = Json.reads[MailToken]
  implicit val tokenJSONReads = Json.reads[Token]
  implicit val mtJSONWrites = Json.writes[MailToken]
  implicit val tokenJSONWrites = Json.writes[Token]

  // implicit val mailtokenJSONReads = (
  //   (JsPath \ "uuid").read[String] and
  //   (JsPath \ "email").read[String] and
  //   (JsPath \ "creationTime").read[String] and
  //   (JsPath \ "expirationTime").read[String] and
  //   (JsPath \ "isSignUp").read[Boolean]
  // )

  // implicit val mailtokenJSONWrites = (
  //   (JsPath \ "uuid").write[String] and
  //   (JsPath \ "email").write[String] and
  //   (JsPath \ "creationTime").write[String] and
  //   (JsPath \ "expirationTime").write[String] and
  //   (JsPath \ "isSignUp").write[Boolean]
  // )

}