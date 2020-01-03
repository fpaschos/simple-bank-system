package gr.fpas.bank.be

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import gr.fpas.bank.be.AccountGroup.{AvailableAccount, RequestAccount}
import gr.fpas.bank.be.AccountHolder.{AccountBalance, Deposit, GetBalance, Withdraw}

import scala.concurrent.Future
import scala.concurrent.duration._

// Some REST dtos
final case class Amount(amount: Double)// {"amount": 23.2}

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

  val routes: Route = pathPrefix("account") {
    concat(
      deposit(),
      withdraw(),
      balance()
    )
  }

  // GET /account/<accountId>
  // returns AvailableAccount(..)
  private def balance(): Route = path(Segment) { accountId =>
    import JsonSupport._

    get {
      val balanceFuture: Future[AccountHolder.Response] =
        group.ask[AccountGroup.Response](replyTo => RequestAccount(accountId, replyTo)) // Ask the accountGroup actor
            .flatMap { // and then
              case AvailableAccount(_, account) => // If success with AvailableAccount ask the accountHolder for its balance
                account.ask[AccountHolder.Response](replyTo => GetBalance(accountId, replyTo))
            }

      onSuccess(balanceFuture) {
        case resp: AccountBalance =>
          complete((StatusCodes.OK, resp))
      }
    }
  }

  // POST /account/<accountId>/deposit
  // returns AvailableAccount(..)
  private def deposit(): Route = path(Segment / "deposit") { accountId =>
    import JsonSupport._

    post {
      entity(as[Amount]) { amount =>
        // Ask for an account AND THEN ask to deposit
        val depositFuture: Future[AccountHolder.Response] =
          group.ask[AccountGroup.Response](replyTo => RequestAccount(accountId, replyTo)) // Ask the accountGroup actor
            .flatMap { // and then
              case AvailableAccount(_, account) => // If success with AvailableAccount ask the accountHolder actor
                account.ask[AccountHolder.Response](replyTo => Deposit(accountId, amount.amount, replyTo))
            }

        onSuccess(depositFuture) {
          case resp@AccountBalance(accountId, balance) =>
            log.info("Deposit [{}]: {} => balance {}", accountId, amount, balance)
            complete((StatusCodes.OK, resp))
        }
      }
    }
  }

  // POST /account/<accountId>/withdraw
  // returns AvailableAccount(..)
  private def withdraw(): Route = path(Segment / "withdraw") { accountId =>
    import JsonSupport._

    post {
      entity(as[Amount]) { amount =>
        // Ask for an account AND THEN ask to withdraw
        val withdrawFuture: Future[AccountHolder.Response] =
          group.ask[AccountGroup.Response](replyTo => RequestAccount(accountId, replyTo)) // Ask the accountGroup actor
            .flatMap {
              case AvailableAccount(_, account) => // If success with AvailableAccount ask the accountHolder actor
                account.ask[AccountHolder.Response](replyTo => Withdraw(accountId, amount.amount, replyTo))
            }

        onSuccess(withdrawFuture) {
          case resp@AccountBalance(accountId, balance) =>
            log.info("Withdraw [{}]: {} => balance {}", accountId, amount, balance)
            complete((StatusCodes.OK, resp))
        }
      }
    }
  }
}




