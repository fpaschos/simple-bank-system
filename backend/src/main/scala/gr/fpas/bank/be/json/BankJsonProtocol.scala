package gr.fpas.bank.be.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import gr.fpas.bank.be.AccountHolder.{AccountBalance, Created, Deposited, Event, Excess, Reserved, TransferCancelled, TransferCompleted, Withdrawed}
import gr.fpas.bank.be.Amount
import gr.fpas.bank.be.domain.Domain.{AccountEventHistory, AccountHistory}
import spray.json._

object BankJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol with ZonedDateTimeProtocol {
  // Events hson  serialization support
  implicit val eventCreatedFormat = jsonFormat5(Created)
  implicit val eventWithdrawedFormat = jsonFormat6(Withdrawed)
  implicit val eventDepositedFormat = jsonFormat6(Deposited)
  implicit val eventReservedFormat = jsonFormat8(Reserved)
  implicit val eventExcessFormat = jsonFormat8(Excess)
  implicit val eventTransferCompletedFormat = jsonFormat8(TransferCompleted)
  implicit val eventTransferCancelledFormat = jsonFormat8(TransferCancelled)



  // ADT hierachy format from
  // https://gist.github.com/jrudolph/f2d0825aac74ed81c92a
  implicit val eventFormat = new RootJsonFormat[Event] {
    def read(json: JsValue): Event =
      json.asJsObject.getFields("type") match {
        case Seq(JsString("Created")) => json.convertTo[Created]
        case Seq(JsString("Withdrawed")) => json.convertTo[Withdrawed]
        case Seq(JsString("Deposited")) => json.convertTo[Deposited]
        case Seq(JsString("Excess")) => json.convertTo[Excess]
        case Seq(JsString("Reserved")) => json.convertTo[Reserved]
        case Seq(JsString("TransferCompleted")) => json.convertTo[TransferCompleted]
        case Seq(JsString("TransferCancelled")) => json.convertTo[TransferCancelled]
      }


    def write(obj: Event): JsValue =
      JsObject((obj match {
        case e: Created => e.toJson
        case e: Withdrawed => e.toJson
        case e: Deposited => e.toJson
      }).asJsObject.fields + ("type" -> JsString(obj.productPrefix)))
  }

  implicit val amountJsonFormat = jsonFormat1(Amount)
  implicit val accountBalanceFormat = jsonFormat5(AccountBalance)
  implicit val accountHistoryFormat = jsonFormat5(AccountHistory)
  implicit val accountEventHistoryFormat = jsonFormat5(AccountEventHistory)
}

