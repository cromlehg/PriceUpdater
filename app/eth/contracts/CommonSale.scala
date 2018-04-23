package eth.contracts

import java.math.BigInteger
import java.util.Arrays
import java.util.Collections
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.RemoteCall
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.Contract
import org.web3j.tx.TransactionManager
import org.web3j.abi.datatypes.Type

class CommonSale(
  contractAddress:    String,
  web3j:              Web3j,
  transactionManager: TransactionManager,
  gasPrice:           BigInteger,
  gasLimit:           BigInteger) extends Contract(
  """
pragma solidity ^0.4.18;

import './math/SafeMath.sol';
import './PercentRateFeature.sol';
import './MintableToken.sol';
import './WalletProvider.sol';
import './InvestedProvider.sol';
import './RetrieveTokensFeature.sol';
import './MintTokensFeature.sol';

contract CommonSale is PercentRateFeature, InvestedProvider, WalletProvider, RetrieveTokensFeature, MintTokensFeature {

  using SafeMath for uint;

  address public directMintAgent;

  uint public price;

  uint public start;

  uint public minInvestedLimit;

  uint public hardcap;

  uint public USDHardcap;
  
  uint public USDPrice;

  uint public ETHtoUSD;

  modifier isUnderHardcap() {
    require(invested <= hardcap);
    _;
  }

  // three digits
  function setUSDHardcap(uint newUSDHardcap) public onlyOwner {
    USDHardcap = newUSDHardcap;
    updateHardcap();
  }

  function updateHardcap() internal {
    hardcap = USDHardcap.mul(1 ether).div(ETHtoUSD);
  }

  function updatePrice() internal {
    price = ETHtoUSD.mul(1 ether).div(USDPrice);
  }

  function setETHtoUSD(uint newETHtoUSD) public onlyDirectMintAgentOrOwner {
    ETHtoUSD = newETHtoUSD;
    updateHardcap();
  }

  // Deprecated!!! Should use setUSDHardcap
  function setHardcap(uint newHardcap) public onlyOwner {
    hardcap = newHardcap;
  }

  modifier onlyDirectMintAgentOrOwner() {
    require(directMintAgent == msg.sender || owner == msg.sender);
    _;
  }

  modifier minInvestLimited(uint value) {
    require(value >= minInvestedLimit);
    _;
  }

  function setStart(uint newStart) public onlyOwner {
    start = newStart;
  }

  function setMinInvestedLimit(uint newMinInvestedLimit) public onlyOwner {
    minInvestedLimit = newMinInvestedLimit;
  }

  function setDirectMintAgent(address newDirectMintAgent) public onlyOwner {
    directMintAgent = newDirectMintAgent;
  }

  function setUSDPrice(uint newUSDPrice) public onlyDirectMintAgentOrOwner {
    USDPrice = newUSDPrice;
    updatePrice();
  }

  // deprecated
  function setPrice(uint newPrice) public onlyDirectMintAgentOrOwner {
    price = newPrice;
  }

  function calculateTokens(uint _invested) internal returns(uint);

  function mintTokensExternal(address to, uint tokens) public onlyDirectMintAgentOrOwner {
    mintTokens(to, tokens);
  }

  function endSaleDate() public view returns(uint);

  function mintTokensByETHExternal(address to, uint _invested) public onlyDirectMintAgentOrOwner returns(uint) {
    updateInvested(_invested);
    return mintTokensByETH(to, _invested);
  }

  function mintTokensByETH(address to, uint _invested) internal isUnderHardcap returns(uint) {
    uint tokens = calculateTokens(_invested);
    mintTokens(to, tokens);
    return tokens;
  }

  function transferToWallet(uint value) internal {
    wallet.transfer(value);
  }

  function updateInvested(uint value) internal {
    invested = invested.add(value);
  }

  function fallback() internal minInvestLimited(msg.value) returns(uint) {
    require(now >= start && now < endSaleDate());
    transferToWallet(msg.value);
    updateInvested(msg.value);
    return mintTokensByETH(msg.sender, msg.value);
  }

  function () public payable {
    fallback();
  }

}

""",
  contractAddress,
  web3j,
  transactionManager,
  gasPrice,
  gasLimit) {

  def queryBigInteger(name: String): RemoteCall[BigInteger] =
    executeRemoteCallSingleValueReturn(new Function(
      name,
      Arrays.asList[Type[_]](),
      Arrays.asList[TypeReference[_]](new TypeReference[Uint256]() {})), classOf[BigInteger])

  def hardcap(): RemoteCall[BigInteger] =
    queryBigInteger("hardcap")

  def price(): RemoteCall[BigInteger] =
    queryBigInteger("price")

  def USDHardcap(): RemoteCall[BigInteger] =
    queryBigInteger("USDHardcap")

  def USDPrice(): RemoteCall[BigInteger] =
    queryBigInteger("USDPrice")

  def ETHtoUSD(): RemoteCall[BigInteger] =
    queryBigInteger("ETHtoUSD")

}