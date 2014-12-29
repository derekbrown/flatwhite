package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.data.Form
import models._
import actions._
import scala.util.Random
import scala.collection.mutable.MutableList
import scala.concurrent.Future
import reactivemongo.api._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson._
import play.modules.reactivemongo.json.BSONFormats._

object Test extends Controller with MongoController{

    def messagesCollection = db.collection[JSONCollection]("messages")
    def usersCollection = db.collection[JSONCollection]("users")
    val testUsers = List(
      Map("firstname" -> "Kelly", "lastname" -> "Boyd", "username" -> "SunshineKelly", "email" -> "kelly@kelly.com"),
      Map("firstname" -> "Gregory", "lastname" -> "Carter", "username" -> "GregCarter", "email" -> "greg@greg.com"),
      Map("firstname" -> "Dylan", "lastname" -> "Gordon", "username" -> "DGordon134", "email" -> "dylan@dylan.com"),
      Map("firstname" -> "Gloria", "lastname" -> "Lawson", "username" -> "GloriaLawson", "email" -> "gloria@gloria.com"),
      Map("firstname" -> "Jeffrey", "lastname" -> "Mason", "username" -> "JeffreyMason", "email" -> "jeffrey@jeffrey.com")
    )
    val testMessages = List(
      ("Drinks After Work?", "Drinks After Work? Fanny pack single-origin coffee pickled, stumptown Helvetica skateboard Intelligentsia Tumblr disrupt kitsch vegan Marfa four dollar toast. VHS High Life Shoreditch pug biodiesel, semiotics crucifix. Squid taxidermy quinoa yr, 90's crucifix tattooed bicycle rights irony mustache wayfarers Schlitz Vice Echo Park. Chambray church-key direct trade Intelligentsia Tumblr Bushwick. Tote bag hoodie pug pickled semiotics authentic squid YOLO. Vice organic keytar, PBR&B beard cold-pressed synth lo-fi gluten-free kogi. Leggings semiotics street art drinking vinegar next level banjo. Mixtape jean shorts +1 synth Bushwick, crucifix mustache kale chips Helvetica hoodie vinyl beard. Forage hella Thundercats, chambray wolf pug hashtag vegan fingerstache shabby chic locavore jean shorts literally. PBR cornhole messenger bag post-ironic narwhal Etsy. Migas Thundercats vinyl lo-fi, readymade actually ugh put a bird on it gastropub tilde beard typewriter cornhole. Post-ironic wolf disrupt next level, paleo Intelligentsia you probably haven't heard of them sriracha XOXO tilde High Life Etsy YOLO readymade. Brunch pour-over jean shorts, shabby chic you probably haven't heard of them 90's viral. YOLO fashion axe vegan, kale chips Helvetica heirloom readymade quinoa VHS squid sustainable cold-pressed whatever roof party."),
      ("Yesterday's Meeting?", "Yesterday's meeting was hoodie fashion axe slow-carb pickled listicle, art party 90's occupy umami Wes Anderson church-key gluten-free chillwave. Quinoa leggings mumblecore, hella tofu Brooklyn tousled wolf bespoke vinyl locavore meggings food truck readymade Pitchfork. Artisan mixtape bicycle rights readymade mlkshk roof party, synth craft beer twee tofu you probably haven't heard of them sustainable cardigan small batch blog. Migas irony authentic, Intelligentsia Odd Future Kickstarter plaid forage Brooklyn Godard tattooed wolf Wes Anderson Etsy. Carles Portland try-hard, keytar Pitchfork squid 90's XOXO. Normcore iPhone flexitarian meditation, Shoreditch Marfa Pinterest chia plaid selfies brunch. Craft beer next level organic, fashion axe meditation Vice Banksy Carles distillery gluten-free keytar sustainable PBR&B fanny pack. IPhone sartorial taxidermy, American Apparel sustainable authentic shabby chic tofu food truck Thundercats Bushwick. Sriracha keytar flannel, pickled Neutra letterpress sartorial lumbersexual four loko Odd Future. Migas pug aesthetic Etsy occupy keytar meh crucifix. Fixie cray jean shorts, ethical synth put a bird on it distillery pickled tousled cliche sriracha bicycle rights. Wes Anderson fixie lumbersexual art party Intelligentsia, street art semiotics meggings hella photo booth Austin master cleanse PBR&B raw denim. Pitchfork Etsy heirloom flexitarian. Hoodie taxidermy salvia, irony sriracha Portland church-key Brooklyn cold-pressed retro organic."),
      ("Next Week's Sprint", "Next week's sprint will be umami cred listicle disrupt, viral skateboard Marfa. DIY Intelligentsia Brooklyn quinoa, artisan pop-up fixie Truffaut try-hard health goth. Hella mumblecore Shoreditch Echo Park blog 8-bit tilde, gluten-free sustainable migas. Quinoa Vice Helvetica, messenger bag DIY umami 90's. Brunch freegan food truck kitsch art party. Flannel Echo Park Neutra distillery mixtape polaroid. Vinyl tote bag heirloom, church-key keffiyeh Pitchfork scenester post-ironic meh selfies Thundercats."),
      ("Customer Feedback from JIRA-0402", "JIRA Ticket 0402 has the following feedback: Ennui sriracha Carles bespoke, trust fund synth retro McSweeney's Odd Future 8-bit hashtag. Pork belly typewriter Austin, sriracha brunch skateboard lo-fi cornhole 90's Pitchfork street art twee messenger bag Williamsburg mumblecore. Actually fashion axe drinking vinegar cronut keytar, narwhal bespoke. Godard literally fanny pack raw denim bicycle rights, scenester dreamcatcher Pitchfork kogi sartorial. Farm-to-table chia 90's craft beer Portland distillery, forage polaroid pickled Austin McSweeney's before they sold out synth. Salvia heirloom Blue Bottle cliche Truffaut Marfa. Hoodie pop-up health goth cornhole vinyl.")
    )

    def createUsers(quantity: Int) = Action.async{
      // TODO: Implement creation loop & randomization of data.
      // This is currently broken (because of the last two lines -> loop to generate list of users works)
      val users: MutableList[User] = MutableList()
      for (x <- 1 to quantity) {
        val userToGenerate = Random.shuffle(testUsers.toList).head
        users += User(BSONObjectID.generate, userToGenerate("firstname"),userToGenerate("lastname"),userToGenerate("username"),userToGenerate("email"))
      }
      val futureUserResult = usersCollection.insert(Json.toJson(users))
      futureUserResult.map(_=> Ok(futureUserResult.toString))
    }

    def createMessages(quantity: Int) = Action.async {
      // TODO: Implement creation loop & randomization of data
      val user1 = User(BSONObjectID.generate, "Kelly","Boyd","SunshineKelly","kelly@kelly.com")
      val user2 = User(BSONObjectID.generate, "Kelly","Boyd","SunshineKelly","kelly@kelly.com")
      val futureU1Result = usersCollection.insert(user1)
      val futureUserResult = usersCollection.insert(user2)
      futureUserResult.map(_=> Ok(futureUserResult.toString))
      val message = Message(BSONObjectID.generate, "Drinks After Work?", "SunshineKelly",Seq(user1._id, user2._id), "Blah blah blah")
      val futureMessageResult = messagesCollection.insert(message)
      futureMessageResult.map(_=> Ok(futureMessageResult.toString))
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
