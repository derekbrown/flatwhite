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
  val logger = Logger("application.controllers.KnotisUserService")
  def users: JSONCollection = db.collection[JSONCollection]("users")
  def tokens: JSONCollection = db.collection[JSONCollection]("tokens")

  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = {
    val cursor: Cursor [User] = users.find(BSONDocument("email"->email, "provider"->providerId)).cursor[User]
    val result = for (user <- cursor.headOption) yield {
      user match {
        case Some(user) => Some(user.profile)
        case None => None
      }
    }
    result
  }

  def find(userId: String, providerId: String): Future[Option[BasicProfile]] = {
    val cursor: Cursor [User] = users.find(BSONDocument("profile.userId"->userId, "profile.providerId"->providerId)).cursor[User]
    val result = for (user <- cursor.headOption) yield {
      user match {
        case Some(user) => Some(user.profile)
        case None => None
      }
    }
    result
  }

  def save(user: BasicProfile, mode: SaveMode): Future[User] = {
    mode match {
      case SaveMode.SignUp =>
        val newUser = User(user)
        val userJson = Json.toJson(newUser)
        users.insert(userJson).map { lastError =>
          Logger.debug(s"Save (New User) - Successfully inserted with LastError: $lastError")
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
    val newToken = Token(token)
    Future.successful {
      tokens.insert(newToken)
      token
    }
  }

  def findToken(uuid: String): Future[Option[MailToken]] = {
    val cursor: Cursor [Token] = tokens.find(BSONDocument("mailToken.uuid" -> uuid)).cursor[Token]
    val result = for (token <- cursor.headOption) yield {
      token match {
        case Some(token) => Some(token.mailToken)
        case None => None
      }
    }
    result
  }

  def deleteToken(uuid: String): Future[Option[MailToken]] = {
    val cursor: Cursor [Token] = tokens.find(BSONDocument("mailToken.uuid" -> uuid)).cursor[Token]
    val result = for (token <- cursor.headOption) yield {
      token match {
        case Some(token) =>
          tokens.remove(BSONDocument("mailToken.uuid" -> uuid))
          Some(token.mailToken)
        case None => None
      }
    }
    result
  }

  def deleteExpiredTokens() {
    val now = new DateTime()
    tokens.remove(BSONDocument("expiration_time"-> BSONDocument( "$lt" -> BSONDateTime(now.getMillis()))))
  }

  def deserializeToken(doc: BSONDocument): Token =
    Token(
      new MailToken(
        doc.getAs[String]("uuid").get,
        doc.getAs[String]("email").get,
        new DateTime(doc.getAs[BSONDateTime]("creation_time").get.value),
        new DateTime(doc.getAs[BSONDateTime]("expiration_time").get.value),
        doc.getAs[Boolean]("isSignUp").get
      )
    )

  def serializeToken(token: Token): BSONDocument =
    BSONDocument(
      "uuid" -> token.mailToken.uuid,
      "email" -> token.mailToken.email,
      "creation_time" -> BSONDateTime(token.mailToken.creationTime.getMillis()),
      "expiration_time" -> BSONDateTime(token.mailToken.expirationTime.getMillis()),
      "isSignUp" -> token.mailToken.isSignUp
    )

  def link(current: User, to: BasicProfile): Future[User] = {
    Future.successful {
      current
    }
  }

  override def updatePasswordInfo(user: User, info: PasswordInfo): Future[Option[BasicProfile]] = {
    val found = users.find(BSONDocument("email"->user.profile.email, "provider"->user.profile.providerId)).cursor[User]
    val result = for (user <- found.headOption) yield {
    // Update MongoDB User Entry with info
    }
    Future.successful {
      Some(user.profile)
    }
  }

  override def passwordInfoFor(user: User): Future[Option[PasswordInfo]] = {
    val found = users.find(BSONDocument("email"->user.profile.email, "provider"->user.profile.providerId)).cursor[User]
    val result = for (user <- found.headOption) yield {
      user.get.profile.passwordInfo
    }
    result
  }
}