package gr.fpas.bank.be

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import gr.fpas.bank.be.AccountGroup.{AccountsList, AvailableAccount, RequestAccount, RequestAccounts}
import gr.fpas.bank.be.AccountHolder.{AccountBalance, Deposit, GetBalance, InsufficientFunds, Withdraw}

import scala.concurrent.Future
import scala.concurrent.duration._

// Some REST dtos
final case class Amount(amount: Double) // {"amount": 23.2}

// Some json serialization configuration
object JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)

  import spray.json.DefaultJsonProtocol._

  implicit val amountJsonFormat = jsonFormat1(Amount)
  implicit val accountBalanceFormat = jsonFormat2(AccountBalance)
}

object AccountApi {
  def apply(group: ActorRef[AccountGroup.Command], system: ActorSystem[_]) = new AccountApi(group, system)
}

class AccountApi(private val group: ActorRef[AccountGroup.Command],
                 private val system: ActorSystem[_]) {

  lazy val log = system.log

  // Needed for ask pattern and Futures
  implicit val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration
  implicit val scheduler = system.scheduler
  implicit val ec = system.executionContext

  val routes: Route = concat(
    accounts(),
    deposit(),
    withdraw(),
    balance())

  // GET /accounts
  // returns AvailableAccount(..)
  private def accounts(): Route = path("accounts") {
    import JsonSupport._
    import spray.json.DefaultJsonProtocol._

    get {
      val f = group.ask[AccountGroup.Response](replyTo => RequestAccounts(replyTo)) // Ask the accountGroup actor

      onSuccess(f) {
        case AccountsList(accounts) =>
          complete((StatusCodes.OK, accounts))
      }
    }
  }


  // GET /account/<id>
  // returns AvailableAccount(..)
  private def balance(): Route = path("account" / Segment) { id =>
    import JsonSupport._

    get {
      val f = group.ask[AccountGroup.Response](replyTo => RequestAccount(id, replyTo)) // Ask the accountGroup actor
        .flatMap { // and then
          case AvailableAccount(_, account) => // If success with AvailableAccount ask the accountHolder for its balance
            account.ask[AccountHolder.Response](replyTo => GetBalance(id, replyTo))
        }

      onSuccess(f) {
        case resp: AccountBalance =>
          complete((StatusCodes.OK, resp))
        case _ =>
          complete(StatusCodes.BadRequest)
      }
    }
  }

  // POST /account/<accountId>/deposit
  // returns AvailableAccount(..)
  private def deposit(): Route = path("account" / Segment / "deposit") { id =>
    import JsonSupport._

    post {
      entity(as[Amount]) { amount =>
        // Ask for an account AND THEN ask to deposit
        val f = group.ask[AccountGroup.Response](replyTo => RequestAccount(id, replyTo)) // Ask the accountGroup actor
          .flatMap { // and then
            case AvailableAccount(_, account) => // If success with AvailableAccount ask the accountHolder actor
              account.ask[AccountHolder.Response](replyTo => Deposit(id, amount.amount, replyTo))
          }

        onSuccess(f) {
          case resp@AccountBalance(accountId, balance) =>
            log.info("Deposit [{}]: {} => balance {}", accountId, amount, balance)
            complete((StatusCodes.OK, resp))
          case _ =>
            complete(StatusCodes.BadRequest)
        }
      }
    }
  }

  // POST /account/<accountId>/withdraw
  // returns AvailableAccount(..)
  private def withdraw(): Route = path("account" / Segment / "withdraw") { id =>
    import JsonSupport._

    post {
      entity(as[Amount]) { amount =>
        // Ask for an account AND THEN ask to withdraw
        val f = group.ask[AccountGroup.Response](replyTo => RequestAccount(id, replyTo)) // Ask the accountGroup actor
          .flatMap {
            case AvailableAccount(_, account) => // If success with AvailableAccount ask the accountHolder actor
              account.ask[AccountHolder.Response](replyTo => Withdraw(id, amount.amount, replyTo))
          }

        onSuccess(f) {
          case resp@AccountBalance(accountId, balance) =>
            log.info("Withdraw [{}]: {} => balance {}", accountId, amount, balance)
            complete((StatusCodes.OK, resp))
          case InsufficientFunds(accountId) =>
            log.error("Withdraw [{}]:  Insufficient funds", accountId)
            complete(StatusCodes.BadRequest)
        }
      }
    }
  }
}




