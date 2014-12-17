package controllers;

import play._;
import play.mvc._;

object Application extends Controller {

    def index = Action {
        return ok(index.render("Your new application is ready."));
    }

}
