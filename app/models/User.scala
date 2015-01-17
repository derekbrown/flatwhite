package models
import play.api.libs.json._
import play.api.libs.functional.syntax._
import reactivemongo.bson._
import play.modules.reactivemongo.json.BSONFormats._
import securesocial.core.{BasicProfile, AuthenticationMethod, OAuth1Info, OAuth2Info, PasswordInfo}

case class User(
  profile: BasicProfile,
  userName: Option[String] = None,
  id: Option[String] = None,
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate)
)

object User {
  def fromWrappedField(wrapperField: String, targetField: String, outField: String) = {
    (__ \ wrapperField \ targetField).readNullable[JsValue].flatMap { targetValue =>
      __.json.update(
        (__ \ outField).json.copyFrom( (__ \ wrapperField \ targetField).json.pick )
      )
    }.orElse(__.json.pickBranch)
  }

  def fromOidField(oidField: String, outField: String) = {
    fromWrappedField(oidField, "$oid", outField)
  }

  implicit object UserBSONReader extends BSONDocumentReader[User] {
    def read(doc:BSONDocument): User =
      User(
        new BasicProfile(
          doc.getAs[String]("providerId").get,
          doc.getAs[String]("userId").get,
          doc.getAs[String]("firstName"),
          doc.getAs[String]("lastName"),
          doc.getAs[String]("fullName"),
          doc.getAs[String]("email"),
          doc.getAs[String]("avatarUrl"),
          new AuthenticationMethod(doc.getAs[String]("authMethod").get)
        ),
        doc.getAs[String]("userName"),
        doc.getAs[String]("id"),
        doc.getAs[BSONObjectID]("_id")
      )
  }

  def encodeIdentity(user: User): BSONDocument = BSONDocument (
    "userid" -> user.profile.userId,
    "provider" -> user.profile.providerId,
    "firstname" -> user.profile.firstName,
    "lastname" -> user.profile.lastName,
    "username" -> user.userName,
    "email" -> user.profile.email,
    "avatar" -> user.profile.avatarUrl,
    "authmethod" -> user.profile.authMethod.method,
    "oauth1" -> (user.profile.oAuth1Info map { oAuth1Info => BSONDocument(
      "token" -> oAuth1Info.token,
      "secret" -> oAuth1Info.secret
    )}),
    "oauth2" -> (user.profile.oAuth2Info map { oAuth2Info => BSONDocument(
      "accessToken" -> oAuth2Info.accessToken,
      "tokenType" -> oAuth2Info.tokenType,
      "expiresIn" -> oAuth2Info.expiresIn,
      "refreshToken" -> oAuth2Info.refreshToken
    )}),
    "password" -> (user.profile.passwordInfo map { passwordInfo => BSONDocument(
      "hasher" -> passwordInfo.hasher,
      "password" -> passwordInfo.password,
      "salt" -> passwordInfo.salt
    )})
  )

  implicit object UserBSONWriter extends BSONDocumentWriter[User] {
    def write(user: User) : BSONDocument = User.encodeIdentity(user)
  }

  implicit val authmethodJSONReads = Json.reads[AuthenticationMethod]
  implicit val oauth1JSONReads = Json.reads[OAuth1Info]
  implicit val oauth2JSONReads = Json.reads[OAuth2Info]
  implicit val passwordJSONReads = Json.reads[PasswordInfo]
  implicit val profileJSONReads = Json.reads[BasicProfile]
  implicit val userJSONReads = fromOidField("_id", "id") andThen Json.reads[User]

  implicit val authmethodJSONWrites = Json.writes[AuthenticationMethod]
  implicit val oauth1JSONWrites = Json.writes[OAuth1Info]
  implicit val oauth2JSONWrites = Json.writes[OAuth2Info]
  implicit val passwordJSONWrites = Json.writes[PasswordInfo]
  implicit val profileJSONWrites = Json.writes[BasicProfile]
  implicit val userJSONWrites = Json.writes[User]
}