package gr.fpas.bank.be.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import gr.fpas.bank.be.AccountHolder.AccountBalance
import gr.fpas.bank.be.Amount
import gr.fpas.bank.be.domain.Domain.AccountHistory
import spray.json.DefaultJsonProtocol

object BankJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol with ZonedDateTimeProtocol {
  implicit val amountJsonFormat = jsonFormat1(Amount)
  implicit val accountBalanceFormat = jsonFormat3(AccountBalance)
  implicit val accountHistoryFormat = jsonFormat5(AccountHistory)
}

