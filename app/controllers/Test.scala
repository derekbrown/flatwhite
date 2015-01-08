package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.data.Form
import play.api.libs.ws.WS
import play.api.libs.functional.syntax._
import models._
import actions._
import scala.util.{Random}
import scala.collection.mutable.MutableList
import scala.concurrent._
import scala.concurrent.duration._
import reactivemongo.api._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson._
import reactivemongo.core.commands._
import play.modules.reactivemongo.json.BSONFormats._

object Test extends Controller with MongoController{

    def messagesCollection = db.collection[JSONCollection]("messages")
    def usersCollection = db.collection[JSONCollection]("users")

    def generateRandomMessage(paragraphs: Int = 2): String = {
      val futureMessage =  WS.url("http://hipsterjesus.com/api/?type=hipster-centric&html=false&paras=" + paragraphs.toString).get().map {response =>
        (response.json \ "text").toString.replace("\"","")
      }
      return Await.result(futureMessage, 1500 milliseconds)
    }

    def generateRandomSubject(words: Int = 3): String = {
      val futureMessage =  WS.url("http://hipsterjesus.com/api/?type=hipster-centric&html=false&paras=1").get().map {response =>
        (response.json \ "text").toString.replace("\"","").replace(",", "").replace("&amp;","").split(" ").take(words).mkString(" ")
      }
      return Await.result(futureMessage, 1500 milliseconds)
    }

    def generateRandomUserData(users: Int = 1): List[JsObject] = {
      val userSet =  WS.url("http://api.randomuser.me/?results=" + users).get()
      val futureUserSet = userSet.map { response =>
        (response.json \\ "user")
      }
      return Await.result(futureUserSet, 2500 milliseconds).asInstanceOf[List[JsObject]]
    }

    def getRandomUser(): Future[Option[User]] = {
      val futureCount = db.command(Count(usersCollection.name))
      return futureCount.flatMap { count =>
        val skip = Random.nextInt(count)
        usersCollection.find(BSONDocument()).options(QueryOpts(skipN = skip)).one[User]
      }
    }

    def getRandomUserID(): Option[BSONObjectID] = {
      val rando = Await.result(getRandomUser(), 2500 milliseconds)
      val randomUser = rando.get
      return randomUser._id
    }

    def getRandomUsername(): String = {
      val rando = Await.result(getRandomUser(), 2500 milliseconds)
      val randomUser = rando.get
      return randomUser.userName
    }

    def createUsers(quantity: Int) = Action {
      val usersToGenerate = generateRandomUserData(quantity)
      val usersList = usersToGenerate.map { user =>
        val userJson = Json.toJson(user)
        User(Some(BSONObjectID.generate), (userJson \\ "first")(0).toString.replace("\"",""), (userJson \\ "last")(0).toString.replace("\"",""), (userJson \\ "username")(0).toString.replace("\"",""), (userJson \\ "email")(0).toString.replace("\"",""))
      }
      val futureUsersResult = usersList.map { user =>
        val userJson = Json.toJson(user)
        val futureUserResult = usersCollection.insert(userJson).map { lastError =>
          Logger.debug(s"Successfully inserted with LastError: $lastError")
          Created
        }
      }
      Ok(Json.obj("users" -> usersList))
    }

    def createMessages(quantity: Int) = Action {
      val messages: MutableList[Message] = MutableList()
      for (x <- 1 to quantity) {
        val randomSubject = generateRandomSubject(Random.nextInt(7)+1)
        val randomMessage = randomSubject + " " + generateRandomMessage(Random.nextInt(3)+1)
        val randomUser = Await.result(getRandomUser(), 1500 milliseconds).get
        val randomUser2 = Await.result(getRandomUser(), 1500 milliseconds).get
        val randomUser3 = Await.result(getRandomUser(), 1500 milliseconds).get
        messages += Message(BSONObjectID.generate, randomSubject, randomUser, Seq(randomUser2, randomUser3), randomMessage)
      }
      val futureMessageResult = messages.map { message =>
        val messageJson = Json.toJson(message)
        val futureMessageResult = messagesCollection.insert(message).map { lastError =>
          Logger.debug(s"Successfully inserted with LastError: $lastError")
          Created
        }
      }
      Ok(Json.obj("messages" -> messages))
    }

    def deleteTestMessages = Action.async {
      val futureMessagesResult = messagesCollection.remove(Json.obj())
      futureMessagesResult.map(_=> Ok(futureMessagesResult.toString))
    }

    def deleteTestUsers = Action.async {
      val futureUsersResult = usersCollection.remove(Json.obj())
      futureUsersResult.map(_=> Ok(futureUsersResult.toString))
    }

}
