package models
import play.api.libs.json._
import play.api.libs.functional.syntax._
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._
import securesocial.core.{Identity,IdentityId,AuthenticationMethod, OAuth1Info, OAuth2Info, PasswordInfo}

case class User(
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  firstName: String,
  lastName: String,
  userName: String,
  email: String,
  avatarUrl: Option[String],
  id: Option[String] = None,
  identityId: IdentityId,
  authMethod: AuthenticationMethod,
  oAuth1Info: Option[OAuth1Info] = None,
  oAuth2Info: Option[OAuth2Info] = None,
  passwordInfo: Option[PasswordInfo] = None
) extends Identity

object IdentityId {
  implicit val identityIdJsonWrites = new Writes[IdentityId] {
    def writes(identityId: IdentityId): JsValue = {
      Json.obj(
        "userId" -> identityId.userId,
        "providerId" -> identityId.providerId
      )
    }
  }

  implicit val identityIdJsonReads = (
    (__ \ 'userId).read[String] and
    (__ \ 'providerId).read[String]
  )
}

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

  implicit val userJSONReads = fromOidField("_id", "id") andThen Json.reads[User]
  implicit val userJSONWrites = Json.writes[User]
}