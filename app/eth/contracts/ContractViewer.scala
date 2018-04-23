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
import org.web3j.tx.ReadonlyTransactionManager

class ContractViewver(config: Config) {

  val wallet = config getString "eth.wallet"

  val contract = config getString "eth.contract"

  val node = config getString "eth.node"

  val service = new HttpService(node)

  val web3 = org.web3j.protocol.Web3j.build(service)

  val txManager = new ReadonlyTransactionManager(web3, wallet)

  val contractImpl = new CommonSale(contract, web3, txManager, BigInteger.valueOf(0), BigInteger.valueOf(0))

  def formatFromETHUint(str: String): String = {
    val bi = new BigInteger(str)
    val divided = bi.divide(new BigInteger("1000000000000000000"))
    divided.toString()
  }

  def price = formatFromETHUint(contractImpl.price().send().toString) + " Tokens per ETH"

  def hadrcap = formatFromETHUint(contractImpl.hardcap().send().toString) + " ETH"

}

