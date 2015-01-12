package models
import play.api.libs.json._
import play.api.libs.functional.syntax._
import reactivemongo.bson._
import play.modules.reactivemongo.json.BSONFormats._
import securesocial.core.{Identity,IdentityId,AuthenticationMethod, OAuth1Info, OAuth2Info, PasswordInfo}

case class User(
  // _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  identityId: IdentityId,
  firstName: String,
  lastName: String,
  fullName: String,
  userName: String,
  email: Option[String],

  avatarUrl: Option[String],
  authMethod: AuthenticationMethod,
  oAuth1Info: Option[OAuth1Info] = None,
  oAuth2Info: Option[OAuth2Info] = None,
  passwordInfo: Option[PasswordInfo] = None,
  id: Option[String] = None,
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate)
) extends Identity

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
        new IdentityId(doc.getAs[String]("userid").get, doc.getAs[String]("provider").get),
        doc.getAs[String]("firstName").get,
        doc.getAs[String]("lastname").get,
        doc.getAs[String]("fullName").get,
        doc.getAs[String]("email").get,
        doc.getAs[String]("avatarUrl"),
        new AuthenticationMethod(doc.getAs[String]("authmethod")),
        doc.getAs[BSONDocument]("oauth1") map { oAuth1Info =>
          new OAuth1Info(
            oAuth1Info.getAs[String]("token").get,
            oAuth1Info.getAs[String]("secret").get
          )
        },
        doc.getAs[BSONDocument]("oauth2") map { oAuth2Info =>
          new OAuth2Info(
            oAuth2Info.getAs[String]("accessToken").get,
            oAuth2Info.getAs[String]("tokenType"),
            oAuth2Info.getAs[Int]("expiresIn"),
            oAuth2Info.getAs[String]("refreshToken")
          )
        },
        doc.getAs[BSONDocument]("password") map { passwordInfo =>
          new PasswordInfo(
            passwordInfo.getAs[String]("hasher").get,
            passwordInfo.getAs[String]("password").get,
            passwordInfo.getAs[String]("salt")
          )
        },
        doc.getAs[String]("id"),
        doc.getAs[BSONObjectID]("_id").get
      )
  }

  def encodeIdentity(id: Identity): BSONDocument = BSONDocument (
    "userid" -> id.identityId.userId,
    "provider" -> id.identityId.providerId,
    "firstname" -> id.firstName,
    "lastname" -> id.lastName,
    "email" -> id.email,
    "avatar" -> id.avatarUrl,
    "authmethod" -> id.authMethod.method,
    "oauth1" -> (id.oAuth1Info map { oAuth1Info => BSONDocument(
      "token" -> oAuth1Info.token,
      "secret" -> oAuth1Info.secret
    )}),
    "oauth2" -> (id.oAuth2Info map { oAuth2Info => BSONDocument(
      "accessToken" -> oAuth2Info.accessToken,
      "tokenType" -> oAuth2Info.tokenType,
      "expiresIn" -> oAuth2Info.expiresIn,
      "refreshToken" -> oAuth2Info.refreshToken
    )}),
    "password" -> (id.passwordInfo map { passwordInfo => BSONDocument(
      "hasher" -> passwordInfo.hasher,
      "password" -> passwordInfo.password,
      "salt" -> passwordInfo.salt
    )})
  )

  implicit object UserBSONWriter extends BSONDocumentWriter[User] {
    def write(user: User) : BSONDocument =
      User.encodeIdentity(user) ++ BSONDocument(
        "fullName" -> user.firstName + " " + user.lastName
      )
  }

  // implicit val userJSONReads = fromOidField("_id", "id") andThen Json.reads[User]
  // implicit val userJSONWrites = Json.writes[User]
}