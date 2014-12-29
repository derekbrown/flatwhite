package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.data.Form
import models._
import actions._
import scala.concurrent.Future
import reactivemongo.api._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson._
import play.modules.reactivemongo.json.BSONFormats._

object Messages extends Controller with MongoController{

    def messagesCollection = db.collection[JSONCollection]("messages")
    def usersCollection = db.collection[JSONCollection]("users")

    def list = WithCors("GET") {Action.async {
      val cursor: Cursor [Message] = messagesCollection.find(Json.obj()).
        sort(Json.obj("_id" -> -1)).
        cursor[Message]

      val futureMessagesList = cursor.collect[List]()
      futureMessagesList.map { messages =>
        var messagesJson = Json.toJson(Json.obj("messages" -> messages))
        Ok(messagesJson)
      }

    }}

    def findBySender(sender: String) = WithCors("GET") {Action.async {
      val cursor: Cursor [Message] = messagesCollection.find(BSONDocument("sender" -> sender)).
        sort(Json.obj("_id" -> -1)).
        cursor[Message]

      val futureMessagesList = cursor.collect[List]()
      futureMessagesList.map { messages =>
        var messagesJson = Json.toJson(Json.obj("messages" -> messages))
        Ok(messagesJson)
      }
    }}

    def create(subject: String, sender: String, participants: Seq[User], messageText: String) = Action.async {
      val pjson = Json.toJson(participants)
      val json = Json.obj(
        "subject" -> subject,
        "sender" -> sender,
        "participants" -> pjson,
        "messageText" -> messageText,
        "created" -> new java.util.Date().getTime()
      )

      messagesCollection.insert(json).map(lastError => Ok("MongoDB Error: %s".format(lastError)))
    }

    def createFromJson = Action.async(parse.json) { request =>
        request.body.validate[Message].map { message =>
        messagesCollection.insert(message).map { lastError =>
          Logger.debug(s"Successfully inserted with LastError: $lastError")
          Created
        }
      }.getOrElse(Future.successful(BadRequest("invalid json")))
    }

    def findByParticipant(participant: String) = Action.async {
      val cursor: Cursor [Message] = messagesCollection.
        find(Json.obj("participants.username" -> participant)).
        sort(Json.obj("created" -> -1)).
        cursor[Message]

      val futureMessagesList = cursor.collect[List]()

      futureMessagesList.map { messages =>
        Ok(Json.toJson(messages))
      }
    }

}
