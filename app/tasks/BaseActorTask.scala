package tasks

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

import com.typesafe.config.Config

import akka.actor.ActorSystem
import eth.contracts.PriceGetter
import eth.contracts.PriceUpdater
import javax.inject.Inject
import models.daos.DAO
import play.Logger
import play.api.libs.ws.WSClient

class BaseActorTask @Inject() (actorSystem: ActorSystem, val dao: DAO, config: Config, ws: WSClient)(implicit executionContext: ExecutionContext) {

  val priceUpdater = new PriceUpdater(config)

  val priceGetter = new PriceGetter(ws)

  actorSystem.scheduler.schedule(initialDelay = 0.seconds, interval = 20.minutes) {
    Logger.debug("Request for price processing...")
    priceGetter.futureResult map { result =>
      Logger.debug("Ethereum price - " + result + "$")
      if (result.indexOf(".") > 0) {
        var toUpdate = result.replace(".", "")
        if (toUpdate.length() < 7) {
          val count = 6 - toUpdate.length
          for (_ <- 1 to count) toUpdate += "0"
        } else if (toUpdate.length > 6) toUpdate = toUpdate.substring(0, 6)
        Logger.debug("To update - " + toUpdate)
        Logger.debug("Transaction hash: " + priceUpdater.updatePrice(toUpdate))
        Logger.debug("Price has been updated")
      }
    }
    Logger.debug("Price processed")
  }

}