package common

import play.api._
import services.UserService

object Global extends play.api.GlobalSettings {
  lazy val userService: UserService = new UserService(play.api.Play.current)
}