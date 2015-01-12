package services

// import ExecutionContext.Implicits.global
import models._
import _root_.java.util.Date
import securesocial.core._
import play.api.Application
import securesocial.core.providers.Token
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import securesocial.core.IdentityId
import securesocial.core.providers.Token
import play.modules.reactivemongo.MongoController
import play.api.mvc.Controller
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.concurrent.Await
import scala.concurrent.duration._
import reactivemongo.api._
import reactivemongo.bson._
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.core.commands.GetLastError
import scala.util.parsing.json.JSONObject
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import play.api.libs.concurrent.Execution.Implicits._

class UserService(application: Application) extends UserServicePlugin(application) with Controller with MongoController{
  def collection: JSONCollection = db.collection[JSONCollection]("users")
  def tokens: JSONCollection = db.collection[JSONCollection]("tokens")

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    val cursor: Cursor [User] = collection.find(BSONDocument("email"->email, "provider"->providerId)).cursor[User]
    Await.result(cursor.headOption, 3 seconds)
  }

  def save(user: Identity): Identity = {
    user match {
      case u: User => collection.save(u)
      case i: Identity => collection.save(User.encodeIdentity(i))
      case _ =>
    }
    user
  }

  def find(id: IdentityId): Option[Identity] = {
    findByEmailAndProvider(id.userId, id.providerId)
  }

  def deserializeToken(doc: BSONDocument): Token =
    Token(
      doc.getAs[String]("uuid").get,
      doc.getAs[String]("email").get,
      new DateTime(doc.getAs[BSONDateTime]("creation_time").get.value),
      new DateTime(doc.getAs[BSONDateTime]("expiration_time").get.value),
      doc.getAs[Boolean]("isSignUp").get
    )

  def serializeToken(token: Token): BSONDocument =
    BSONDocument(
      "uuid" -> token.uuid,
      "email" -> token.email,
      "creation_time" -> BSONDateTime(token.creationTime.getMillis()),
      "expiration_time" -> BSONDateTime(token.expirationTime.getMillis()),
      "isSignUp" -> token.isSignUp
    )

  def save(token: Token) {
    tokens.save(serializeToken(token))
  }

  def findToken(uuid: String): Option[Token] = {
    val cursor = tokens.find(BSONDocument("uuid"->uuid)).cursor[BSONDocument]
    Await.result(cursor.headOption, 3 seconds).map {
      d:BSONDocument => deserializeToken(d)
    }
  }

  def deleteToken(uuid: String) {
    tokens.remove(BSONDocument("uuid"->uuid))
  }

  def deleteExpiredTokens() {
    val now = new DateTime()
    tokens.remove(BSONDocument("expiration_time"-> BSONDocument( "$lt" -> BSONDateTime(now.getMillis()))))
  }
}