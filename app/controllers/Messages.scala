package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.data.Form
import models._
import actions._
import models.JsonFormats._
import scala.concurrent.Future
import reactivemongo.api._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson._
import play.modules.reactivemongo.json.BSONFormats._

object Messages extends Controller with MongoController{

    def collection = db.collection[JSONCollection]("messages")

    def list = WithCors("GET") {Action.async {
      val cursor: Cursor [Message] = collection.find(Json.obj()).
        sort(Json.obj("created" -> -1)).
        cursor[Message]

      val futureMessagesList = cursor.collect[List]()

      futureMessagesList.map { messages =>
        Ok(Json.toJson(Json.obj("messages" -> messages)))
      }
    }}

    def create(subject: String, sender: String, participants: List[User], messageText: String) = Action.async {
      val pjson = Json.toJson(participants)
      val json = Json.obj(
        "subject" -> subject,
        "sender" -> sender,
        "participants" -> pjson,
        "messageText" -> messageText,
        "created" -> new java.util.Date().getTime()
      )

      collection.insert(json).map(lastError => Ok("MongoDB Error: %s".format(lastError)))
    }

    def createTest = Action.async {
      // Clean this crap up.
      val user1id = BSONObjectID.generate.stringify
      val user2id = BSONObjectID.generate.stringify
      val user1 = User(user1id, "Kelly","Boyd","SunshineKelly","kelly@kelly.com")
      val user2 = User(user2id, "Kelly","Boyd","SunshineKelly","kelly@kelly.com")
      val futureU1Result = collection.insert(user1id)
      val futureU2Result = collection.insert(user2id)
      val message = Message(BSONObjectID.generate.stringify, "Meeting Today About Project Bermuda", "GregCarter",List(user1id, user2id), "Blah blah blah")
      val futureResult = collection.insert(message)
      futureResult.map(_=> Ok(message.toString))
    }

    def createFromJson = Action.async(parse.json) { request =>
        request.body.validate[Message].map { message =>
        collection.insert(message).map { lastError =>
          Logger.debug(s"Successfully inserted with LastError: $lastError")
          Created
        }
      }.getOrElse(Future.successful(BadRequest("invalid json")))
    }

    def deleteTest = Action.async {
      val futureResult = collection.remove(Json.obj("sender" -> "GregCarter"))
      futureResult.map(_=> Ok(futureResult.toString))
    }

    def findByParticipant(participant: String) = Action.async {
      val cursor: Cursor [Message] = collection.
        find(Json.obj("participants.username" -> participant)).
        sort(Json.obj("created" -> -1)).
        cursor[Message]

      val futureMessagesList = cursor.collect[List]()

      futureMessagesList.map { messages =>
        Ok(Json.toJson(messages))
      }
    }

}
