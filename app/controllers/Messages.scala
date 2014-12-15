package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.data.Form
import models._
import models.JsonFormats._
import scala.concurrent.Future
import reactivemongo.api._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import views.html._

object Messages extends Controller with MongoController{

    def collection: JSONCollection = db.collection[JSONCollection]("messages")

    def list = Action.async {
      val cursor: Cursor [Message] = collection.find(Json.obj()).
        sort(Json.obj("created" -> -1)).
        cursor[Message]

      val futureMessagesList: Future[List[Message]] = cursor.collect[List]()

      futureMessagesList.map { messages =>
        Ok(Json.toJson(messages))
      }
    }

    def create(subject: String, sender: String, participants: Seq[User], message: String) = Action.async {
      val pjson = Json.toJson(participants)
      val json = Json.obj(
        "subject" -> subject,
        "sender" -> sender,
        "participants" -> pjson,
        "message" -> message,
        "created" -> new java.util.Date().getTime()
      )

      collection.insert(json).map(lastError => Ok("MongoDB Error: %s".format(lastError)))
    }

    def createTest = Action.async {
      val message = Message("Meeting Today About Project Bermuda", "GregCarter",Seq(
          User("Kelly","Boyd","SunshineKelly","kelly@kelly.com"),
          User("Gregory","Carter","GregCarter","greg@greg.com"),
          User("Dylan","Gordon","DGordon134","dylan@dylan.com"),
          User("Jeffrey","Mason","JeffreyMason","jeffrey@jeffrey.com"),
          User("Gloria","Lawson","GloriaLawson","gloria@gloria.com")), "Blah blah blah")
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

      val futureMessagesList: Future[List[Message]] = cursor.collect[List]()

      futureMessagesList.map { messages =>
        Ok(Json.toJson(messages))
      }
    }

}
