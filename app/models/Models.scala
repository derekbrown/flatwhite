package models
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import reactivemongo.bson._
import play.modules.reactivemongo.json.BSONFormats._

object JsonExtensions {

  import play.api.libs.json._

  def withDefault[A](key: String, default: A)(implicit writes: Writes[A]) = __.json.update((__ \ key).json.copyFrom((__ \ key).json.pick orElse Reads.pure(Json.toJson(default))))
  def copyKey(fromPath: JsPath,toPath:JsPath ) = __.json.update(toPath.json.copyFrom(fromPath.json.pick))
  def copyOptKey(fromPath: JsPath,toPath:JsPath ) = __.json.update(toPath.json.copyFrom(fromPath.json.pick orElse Reads.pure(JsNull)))
  def moveKey(fromPath:JsPath, toPath:JsPath) =(json:JsValue)=> json.transform(copyKey(fromPath,toPath) andThen fromPath.json.prune).get
}

case class User(
  _id: BSONObjectID,
  firstName: String,
  lastName: String,
  userName: String,
  email: String
)

object User {
  import JsonExtensions._
  implicit val UserFormat = new Format[User]{
    val base = Json.format[User]
    private val publicIdPath: JsPath = JsPath \ 'id
    private val privateIdPath: JsPath = JsPath \ '_id
    private val privateWritePath: JsPath = JsPath \ '_id \ '$oid

    def reads(json: JsValue): JsResult[User] = base.compose(copyKey(publicIdPath,privateIdPath)).reads(json)
    def writes(o: User): JsValue = base.transform(moveKey(privateWritePath,publicIdPath)).writes(o)
  }
  implicit val UserReads: Reads[User] = (
    (JsPath \ "id").read[BSONObjectID] and
    (JsPath \ "firstName").read[String] and
    (JsPath \ "lastName").read[String] and
    (JsPath \ "userName").read[String] and
    (JsPath \ "email").read[String]
  )(User.apply _)

  implicit val UserWrites: Writes[User] = (
    (JsPath \ "id").write[BSONObjectID] and
    (JsPath \ "firstName").write[String] and
    (JsPath \ "lastName").write[String] and
    (JsPath \ "userName").write[String] and
    (JsPath \ "email").write[String]
  )(unlift(User.unapply))
}

case class Message(
  _id: BSONObjectID,
  subject: String,
  sender: String,
  participants: Seq[BSONObjectID],
  messageText: String
)

object Message {
  import JsonExtensions._
  implicit val MessageFormat = new Format[Message]{
    val base = Json.format[Message]
    private val publicIdPath: JsPath = JsPath \ 'id
    private val privateIdPath: JsPath = JsPath \ '_id
    private val privateWritePath: JsPath = JsPath \ '_id \ '$oid
    def reads(json: JsValue): JsResult[Message] = base.compose(copyKey(publicIdPath,privateIdPath)).reads(json)
    def writes(o: Message): JsValue = base.transform(moveKey(privateWritePath,publicIdPath)).writes(o)
  }
  implicit val messageReads: Reads[Message] = (
    (JsPath \ "id").read[BSONObjectID] and
    (JsPath \ "subject").read[String] and
    (JsPath \ "sender").read[String] and
    (JsPath \ "participants").read[Seq[BSONObjectID]] and
    (JsPath \ "messageText").read[String]
  )(Message.apply _)

  implicit val messageWrites: Writes[Message] = (
    (JsPath \ "id").write[BSONObjectID] and
    (JsPath \ "subject").write[String] and
    (JsPath \ "sender").write[String] and
    (JsPath \ "participants").write[Seq[BSONObjectID]] and
    (JsPath \ "messageText").write[String]
  )(unlift(Message.unapply))
}