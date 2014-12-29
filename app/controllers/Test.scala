package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.data.Form
import play.api.libs.ws.WS
import models._
import actions._
import scala.util.Random
import scala.collection.mutable.MutableList
import scala.concurrent._
import scala.concurrent.duration._
import reactivemongo.api._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson._
import play.modules.reactivemongo.json.BSONFormats._

object Test extends Controller with MongoController{

    def messagesCollection = db.collection[JSONCollection]("messages")
    def usersCollection = db.collection[JSONCollection]("users")

    // TODO: Can I make this test data cleaner by using my model classes instead of Maps?
    // TODO: Also, is there a service I can use in place of hard-coded data?
    //       - Messages & subjects done.
    //       - Need to find a service to generate random names, usernames, emails.

    def generateRandomMessage(paragraphs: Int = 2): String = {
      val futureMessage =  WS.url("http://hipsterjesus.com/api/?type=hipster-centric&html=false&paras=" + paragraphs.toString).get().map {response =>
        (response.json \ "text").toString.replace("\"","")
      }
      return Await.result(futureMessage, 1000 milliseconds)
    }

    def generateRandomSubject(words: Int = 3): String = {
      val futureMessage =  WS.url("http://hipsterjesus.com/api/?type=hipster-centric&html=false&paras=1").get().map {response =>
        (response.json \ "text").toString.replace("\"","").replace(",", "").replace("&amp;","").split(" ").take(words).mkString(" ")
      }
      return Await.result(futureMessage, 1000 milliseconds)
    }

    def createUsers(quantity: Int) = Action {
      val users: MutableList[User] = MutableList()
      for (x <- 1 to quantity) {
        val userToGenerate = Random.shuffle(testUsers.toList).head
        users += User(BSONObjectID.generate, userToGenerate("firstname"),userToGenerate("lastname"),userToGenerate("username"),userToGenerate("email"))
      }
      val futureUsersResult = users.map { user =>
        val userJson = Json.toJson(user)
        val futureUserResult = usersCollection.insert(userJson).map { lastError =>
          Logger.debug(s"Successfully inserted with LastError: $lastError")
          Created
        }
      }
      Ok(Json.obj("users" -> users))

    }

    def createMessages(quantity: Int) = Action {
      // TODO: Implement randomization of users attached to messages as sender & participants. Need getRandomUsername and getRandomUserID functions.
      val user1 = User(BSONObjectID.generate, "Kelly","Boyd","SunshineKelly","kelly@kelly.com")
      val futureUserResult = usersCollection.insert(user1)
      futureUserResult.map(_=> Ok(futureUserResult.toString))

      val messages: MutableList[Message] = MutableList()
      for (x <- 1 to quantity) {
        val messageToGenerate = Random.shuffle(testMessages.toList).head
        val randomSubject = generateRandomSubject(Random.nextInt(7)+1)
        val randomMessage = randomSubject + " " + generateRandomMessage(Random.nextInt(3)+1)
        messages += Message(BSONObjectID.generate, randomSubject, user1.userName, Seq(user1._id), randomMessage)
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
