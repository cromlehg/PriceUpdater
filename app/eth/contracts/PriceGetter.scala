package eth.contracts

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject
import play.api.libs.ws.WSClient
import play.Logger

class PriceGetter @Inject() (ws: WSClient)(implicit ec: ExecutionContext) {

  val url = "https://api.coinmarketcap.com/v1/ticker/ethereum/?convert=USD"

  val futureResult: Future[String] = ws.url(url).get().map {
    response =>
      (response.json \ 0 \ "price_usd").as[String]
  }

}