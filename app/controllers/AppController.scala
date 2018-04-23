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
import eth.contracts.ContractViewver
import eth.contracts.PriceGetter
import play.api.libs.ws.WSClient
import play.Logger
import eth.contracts.PriceUpdater

@Singleton
class AppController @Inject() (cc: ControllerComponents, dao: DAO, config: Config, ws: WSClient)(implicit ec: ExecutionContext)
  extends Authorizable(cc, dao, config) {

  import scala.concurrent.Future.{ successful => future }

  val contractViewer = new ContractViewver(config)

  val priceUpdater = new PriceUpdater(config)

  val priceGetter = new PriceGetter(ws)

  def index = Action.async { implicit request =>
    implicit val ac = new AppContext()
    optionalAuthorized { accountOpt =>
      priceGetter.futureResult flatMap { price =>
        future(Ok(views.html.app.index(
          contractViewer.price,
          contractViewer.hadrcap,
          price,
          contractViewer.contract)))
      }
    }
  }

  def forceUpdate() = Action.async { implicit request =>
    implicit val ac = new AppContext()
    onlyAuthorized { account =>
      Logger.debug("Force price update processing...")
      priceGetter.futureResult map { result =>
        Logger.debug("Ethereum price - " + result + "$")
        val toUpdate = result.replace(".", "")
        Logger.debug("To update - " + toUpdate)
        val hash = priceUpdater.updatePrice(toUpdate)
        Logger.debug("Transaction hash: " + priceUpdater.updatePrice(toUpdate))
        Redirect(routes.AppController.index).flashing("success" -> ("Price update transaction sendind with hash " + hash))
      }
    }
  }

}
