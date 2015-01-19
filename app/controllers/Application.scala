package controllers;

import play.api._
import play.api.mvc._
import securesocial.core._
import models.User

class Application(override implicit val env: RuntimeEnvironment[User]) extends securesocial.core.SecureSocial[User] {

  def index = Action {
    Ok(views.html.index("Flatwhite"));
  }

}
