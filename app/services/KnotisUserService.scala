package services

import models._
import _root_.java.util.Date
import securesocial.core._
import play.api._
import play.api.Application
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import securesocial.core.providers.{UsernamePasswordProvider, MailToken}
import securesocial.core.services.{UserService, SaveMode}
import play.modules.reactivemongo.MongoController
import play.api.mvc.Controller
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import reactivemongo.api._
import reactivemongo.bson._
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.core.commands.GetLastError
import scala.util.parsing.json.JSONObject
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import play.api.libs.concurrent.Execution.Implicits._

class KnotisUserService(application: Application) extends UserService[User] with Controller with MongoController{
  def users: JSONCollection = db.collection[JSONCollection]("users")
  def tokens: JSONCollection = db.collection[JSONCollection]("tokens")

  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = {
    val cursor: Cursor [User] = users.find(BSONDocument("email"->email, "provider"->providerId)).cursor[User]
    val result = for (user <- cursor.headOption) yield {
      Some(user.get.profile)
    }
    result
  }

  def find(id: BasicProfile): Future[Option[BasicProfile]] = {
    findByEmailAndProvider(id.userId, id.providerId)
  }

  def save(user: BasicProfile, mode: SaveMode): Future[User] = {
    mode match {
      case SaveMode.SignUp =>
        val newUser = User(user)
        users.insert(newUser).map { lastError =>
          Logger.debug(s"Successfully inserted with LastError: $lastError")
          newUser
        }
      case SaveMode.LoggedIn =>
        Future.successful {
          User(user)
        }
      // TODO: Save updated data.
    }
  }

  def saveToken(token: MailToken): Future[MailToken] = {
    Future.successful {
      tokens.insert(token)
      token
    }
  }

  def findToken(token: String): Future[Option[MailToken]] = {
    val cursor = tokens.find(BSONDocument("uuid" -> token)).cursor[BSONDocument]
    val result = for (token <- cursor.headOption) yield {
      token
    }
    result
  }

  def deleteToken(uuid: String): Future[Option[MailToken]] = {
    Future.successful {
      tokens.remove(BSONDocument("uuid"->uuid))
    }
  }

  def deleteExpiredTokens() {
    val now = new DateTime()
    tokens.remove(BSONDocument("expiration_time"-> BSONDocument( "$lt" -> BSONDateTime(now.getMillis()))))
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
}