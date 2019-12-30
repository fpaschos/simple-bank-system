package gr.fpas.bank.be

import akka.actor.typed.ActorSystem
import gr.fpas.bank.be.AccountGroup.RequestAccount
import gr.fpas.bank.be.AccountHolder.{AccountBalance, Deposit, GetBalance, Response, Withdraw}

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

// Needed for ask pattern
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout

import scala.concurrent.duration._


/**
 * This is the "main" class called Runner
 */
object Runner extends App {
  val accountId = "ACC_1"

  val system = ActorSystem(AccountGroup.create(), "bank-backend-system")

  // Ask the current balance via a Future
  // see docs https://doc.akka.io/docs/akka/current/typed/interaction-patterns.html#request-response-with-ask-from-outside-an-actor

  // Needed for Future
  // the response callback will be executed on this execution context and scheduler
  implicit val scheduler = system.scheduler
  implicit val ec = system.executionContext

  // asking someone requires a timeout if the timeout hits without response
  // the ask is failed with a TimeoutException
  implicit val timeout: Timeout = 3.seconds

  // Create an account AND THEN ask the balance
  val groupFuture: Future[AccountGroup.Response] = system.ref.ask[AccountGroup.Response](replyTo => RequestAccount(accountId, replyTo))

  val balanceFuture: Future[Response] = groupFuture.flatMap(response => {
    response match {
      case AccountGroup.AvailableAccount(accountId, account) =>

        account ! Deposit(accountId, 100)
        account.ask(replyTo => GetBalance(accountId, replyTo))
    }
  })

  // Blocks the thread until the future is completed
  val  balance: Response = Await.result(balanceFuture, timeout.duration)
  println(s"Balance result is $balance")


  // Ask again for the account list
  val accountListFuture: Future[AccountGroup.Response] = system.ref.ask[AccountGroup.Response](replyTo => AccountGroup.RequestAccounts(replyTo))
  val accountList = Await.result(accountListFuture, timeout.duration)

  println(s"Account list result is $accountList")


  // Close the system
  system.terminate()
}
