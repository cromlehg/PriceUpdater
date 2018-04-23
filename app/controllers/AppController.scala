package controllers

import scala.concurrent.ExecutionContext

import javax.inject.Inject
import javax.inject.Singleton
import models.daos.DAO
import play.api.i18n.I18nSupport
import play.api.mvc.AbstractController
import play.api.mvc.AnyContent
import play.api.mvc.ControllerComponents
import play.api.mvc.Request
import com.typesafe.config.Config

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class AppController @Inject() (cc: ControllerComponents, dao: DAO, config: Config)(implicit ec: ExecutionContext)
  extends Authorizable(cc, dao, config) {

  import scala.concurrent.Future.{ successful => future }

  def index = Action.async { implicit request =>
    implicit val ac = new AppContext()
    future(Ok(views.html.app.index()))
  }

  def controlForceUpdate() = Action.async { implicit request =>
    implicit val ac = new AppContext()
    future(Ok(views.html.app.forceUpdate()))
  }

}
