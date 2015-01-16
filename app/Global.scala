package common

import models._
import play.api._
import securesocial.core.RuntimeEnvironment
import services.KnotisUserService

object Global extends play.api.GlobalSettings {

  object KnotisRuntimeEnvironment extends RuntimeEnvironment.Default[User] {
    lazy val userService: KnotisUserService = new KnotisUserService(play.api.Play.current)
  }

}