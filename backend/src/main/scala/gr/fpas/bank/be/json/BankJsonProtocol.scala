package gr.fpas.bank.be.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import gr.fpas.bank.be.AccountHolder.{AccountBalance, Created, Deposited, Event, Withdrawed}
import gr.fpas.bank.be.Amount
import gr.fpas.bank.be.domain.Domain.{AccountEventHistory, AccountHistory}
import spray.json._

object BankJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol with ZonedDateTimeProtocol {
  // Events hson  serialization support
  implicit val eventCreatedFormat = jsonFormat3(Created)
  implicit val eventWithdrawedFormat = jsonFormat4(Withdrawed)
  implicit val eventDepositedFormat = jsonFormat4(Deposited)


  // ADT hierachy format from
  // https://gist.github.com/jrudolph/f2d0825aac74ed81c92a
  implicit val eventFormat = new RootJsonFormat[Event] {
    def read(json: JsValue): Event =
      json.asJsObject.getFields("type") match {
        case Seq(JsString("Created")) => json.convertTo[Created]
        case Seq(JsString("Withdrawed")) => json.convertTo[Withdrawed]
        case Seq(JsString("Deposited")) => json.convertTo[Deposited]
      }


    def write(obj: Event): JsValue =
      JsObject((obj match {
        case e: Created => e.toJson
        case e: Withdrawed => e.toJson
        case e: Deposited => e.toJson
      }).asJsObject.fields + ("type" -> JsString(obj.productPrefix)))
  }

  implicit val amountJsonFormat = jsonFormat1(Amount)
  implicit val accountBalanceFormat = jsonFormat3(AccountBalance)
  implicit val accountHistoryFormat = jsonFormat5(AccountHistory)
  implicit val accountEventHistoryFormat = jsonFormat5(AccountEventHistory)
}

