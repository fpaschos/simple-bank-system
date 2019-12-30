package gr.fpas.bank.be

import akka.actor.typed.ActorSystem
import gr.fpas.bank.be.AccountHolder.{AccountBalance, Deposit, GetBalance, Response, Withdraw}

import scala.concurrent.Future
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

  val system = ActorSystem[AccountHolder.Command](AccountHolder.create(accountId), "bank-backend-system")

  system ! Deposit(accountId, 2) // The notation "!" is the same as ".tell(...)" in scala
  system ! Deposit(accountId, 3)
  system ! Withdraw(accountId, 4)

  // Ask the current balance via a Future
  // see docs https://doc.akka.io/docs/akka/current/typed/interaction-patterns.html#request-response-with-ask-from-outside-an-actor

  // Needed for Future
  // the response callback will be executed on this execution context and scheduler
  implicit val scheduler = system.scheduler
  implicit val ec = system.executionContext


  // asking someone requires a timeout if the timeout hits without response
  // the ask is failed with a TimeoutException
  implicit val timeout: Timeout = 3.seconds


  // Ask the final balance and close the system
  val balance: Future[Response] = system.ref.ask[Response](ref => GetBalance(accountId, ref))
  balance.onComplete {

    case Success(AccountBalance(accountId, balance)) =>  // This is pattern matching
      println(s"Future Response: Account $accountId balance is $balance")
      system.terminate()

    case Failure(ex) =>
      println(s"BOOM! something is wrong: ${ex.getMessage}")
      system.terminate()
  }
}
