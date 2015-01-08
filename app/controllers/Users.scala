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
        sort(Json.obj("lastName" -> 1)).
        cursor[User]

      val futureUserList = cursor.collect[List]()
      futureUserList.map { users =>
        var usersJson = Json.toJson(Json.obj("users" -> users))
        Ok(usersJson)
      }

    }}

    def saveUser = Action(parse.json) { request =>
      val userJson = request.body.validate[User].map { user =>
        collection.insert(user).map { lastError =>
          Logger.debug(s"Successfully inserted with LastError: $lastError")
          Created
        }
      }
      // }.getOrElse(Future.successful(BadRequest("Invalid JSON")))
      // userResult.fold(
      //   errors => {
      //     BadRequest(Json.obj("status"->"Nope", "message"->JsError.toFlatJson(errors)))
      //   },
      //   user => {
      //     collection.insert(user)
      //   }
      // )
      Ok(userJson.toString)
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
