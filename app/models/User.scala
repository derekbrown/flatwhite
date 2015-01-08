package models
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._

case class User(
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  firstName: String,
  lastName: String,
  userName: String,
  email: String,
  id: Option[String] = None
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

  implicit val userJSONReads = fromOidField("_id", "id") andThen Json.reads[User]
  implicit val userJSONWrites = Json.writes[User]
}