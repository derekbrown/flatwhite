import models._
import play.api.GlobalSettings
import securesocial.core.RuntimeEnvironment
import securesocial.core.providers._
import securesocial.core.providers.utils.{Mailer, PasswordHasher, PasswordValidator}
import services.KnotisUserService
import java.lang.reflect.Constructor
import scala.collection.immutable.ListMap

object Global extends play.api.GlobalSettings {

  object KnotisRuntimeEnvironment extends RuntimeEnvironment.Default[User] {
    override lazy val userService: KnotisUserService = new KnotisUserService(play.api.Play.current)
    override lazy val providers = ListMap(
      include(new UsernamePasswordProvider[User](userService, avatarService, viewTemplates, passwordHashers))
    )
  }

  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    val instance = controllerClass.getConstructors.find { c =>
      val params = c.getParameterTypes
      params.length == 1 && params(0) == classOf[RuntimeEnvironment[User]]
    }.map {
      _.asInstanceOf[Constructor[A]].newInstance(KnotisRuntimeEnvironment)
    }
    instance.getOrElse(super.getControllerInstance(controllerClass))
  }

}