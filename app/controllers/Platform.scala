package controllers;

import play.api._
import play.api.mvc._
import actions._
import reactivemongo.api._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

object Platform extends Controller with MongoController{

    def login = WithCors("GET", "POST") {Action {
        Ok("Logged In");
    }}

}
