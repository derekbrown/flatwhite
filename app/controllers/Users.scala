package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import models._
import actions._
import scala.concurrent.Future
import reactivemongo.api._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson._
import play.modules.reactivemongo.json.BSONFormats._

object Users extends Controller with MongoController{

    def collection = db.collection[JSONCollection]("users")

    def list = WithCors("GET") {Action.async {
      val cursor: Cursor [User] = collection.find(Json.obj()).
        sort(Json.obj("_id" -> -1)).
        cursor[User]

      val futureUserList = cursor.collect[List]()
      futureUserList.map { users =>
        var usersJson = Json.toJson(Json.obj("users" -> users))
        Ok(usersJson)
      }

    }}

    def saveUser = Action(BodyParsers.parse.json) { request =>
      val userResult = request.body.validate[User]
      userResult.fold(
        errors => {
          BadRequest(Json.obj("status"->"Nope", "message"->JsError.toFlatJson(errors)))
        },
        user => {
          collection.insert(user)
        }
      )

      Ok(userResult.toString)
    }

    def create(firstname: String, lastname: String, username: String, email: String) = Action.async {
      val json = Json.obj(
        "_id" -> BSONObjectID.generate,
        "firstname" -> firstname,
        "lastname" -> lastname,
        "username" -> username,
        "email" -> email,
        "created" -> new java.util.Date().getTime()
      )

      collection.insert(json).map(lastError => Ok("MongoDB Error: %s".format(lastError)))
    }

    def createTest = Action.async {
      val testuser = User(BSONObjectID.generate, "Kelly","Boyd","SunshineKelly","kelly@kelly.com")
      val futureResult = collection.insert(testuser)
      futureResult.map(_=> Ok(testuser.toString))
    }

    def createFromJson = Action.async(parse.json) { request =>
        request.body.validate[User].map { user =>
        collection.insert(user).map { lastError =>
          Logger.debug(s"Successfully inserted with LastError: $lastError")
          Created
        }
      }.getOrElse(Future.successful(BadRequest("invalid json")))
    }

    def findByUsername(username: String) = Action.async {
      val cursor: Cursor [User] = collection.
        find(Json.obj("username" -> username)).
        sort(Json.obj("created" -> -1)).
        cursor[User]

      val futureUsersList = cursor.collect[List]()

      futureUsersList.map { users =>
        Ok(Json.toJson(users))
      }
    }

}
