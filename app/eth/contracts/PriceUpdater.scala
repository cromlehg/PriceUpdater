package eth.contracts

import org.web3j.tx.FastRawTransactionManager
import org.web3j.protocol.http.HttpService
import org.web3j.crypto.Credentials
import java.util.Arrays
import java.math.BigInteger
import java.util.Collections
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.abi.FunctionEncoder
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.utils.Numeric
import com.typesafe.config.Config

class PriceUpdater(config: Config) {

  val wallet = config getString "eth.wallet"

  val contract = config getString "eth.contract"
                                   
  val pkey = config getString "eth.pkey"
  
  val node = config getString "eth.node"

  val service = new HttpService(node)

  val web3 = org.web3j.protocol.Web3j.build(service)

  val credentials = Credentials.create(pkey)

  val txManager = new FastRawTransactionManager(web3, credentials)

  def updatePrice(price: String): String = {

    val function = new org.web3j.abi.datatypes.Function(
      "setETHtoUSD",
      Arrays.asList[org.web3j.abi.datatypes.Type[_]](new org.web3j.abi.datatypes.Uint(new BigInteger(price))),
      Collections.emptyList[org.web3j.abi.TypeReference[_]]())

    val nonce = web3.ethGetTransactionCount(wallet, DefaultBlockParameterName.LATEST).send().getTransactionCount()

    val encodedFunction = FunctionEncoder.encode(function)

    val transaction = RawTransaction.createTransaction(nonce, new BigInteger("7000000000"), new BigInteger("100000"), contract, BigInteger.ZERO, encodedFunction)

    val signedMessage = TransactionEncoder.signMessage(transaction, credentials)

    val hexValue = Numeric.toHexString(signedMessage)

    val ethSendTransaction = web3.ethSendRawTransaction(hexValue).sendAsync().get()

    ethSendTransaction.getTransactionHash()

  }

}

