package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.data.Form
import play.api.data.Forms._
import models._
import models.JsonFormats._
import scala.concurrent.Future
import reactivemongo.api._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import views.html._

object Users extends Controller with MongoController{

    def collection: JSONCollection = db.collection[JSONCollection]("users")

    def list = Action.async {
      val cursor: Cursor [User] = collection.find(Json.obj()).
        sort(Json.obj("created" -> -1)).
        cursor[User]

      val futureUsersList: Future[List[User]] = cursor.collect[List]()

      futureUsersList.map { users =>
        Ok(Json.toJson(users))
      }
    }

    def create(firstname: String, lastname: String, username: String) = Action.async {
      val json = Json.obj(
        "firstname" -> firstname,
        "lastname" -> lastname,
        "username" -> username,
        "created" -> new java.util.Date().getTime()
      )

      collection.insert(json).map(lastError => Ok("MongoDB Error: %s".format(lastError)))
    }

    def createUser = Action.async {
      val user = Form(mapping(
          "firstname" -> text,
          "lastname" -> text,
          "username" -> text,
          "email" -> text
        )(User.apply)(User.unapply))
      val futureResult = collection.insert(user.bindFromRequest.get)
      futureResult.map(_=> Ok(user.toString))
    }

    def createTest = Action.async {
      val testuser = User("Kelly","Boyd","SunshineKelly","kelly@kelly.com")
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

      val futureUsersList: Future[List[User]] = cursor.collect[List]()

      futureUsersList.map { users =>
        Ok(Json.toJson(users))
      }
    }

}
