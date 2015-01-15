package controllers;

import play.api._
import play.api.mvc._
import services.UserService

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Flatwhite"));
  }

}
