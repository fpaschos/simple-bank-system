package gr.fpas.bank.be

import akka.actor.typed.{ActorRef, ActorSystem}
import gr.fpas.bank.be.AccountHolder.{Deposit, GetBalance, Response, Withdraw}

/**
 * This is the "main" class called Runner
 */
object Runner extends App {
  val accountId = "ACC_1"

  val system = ActorSystem[AccountHolder.Command](AccountHolder.create(accountId), "bank-backend-system")

  system.tell(Deposit(accountId, 2))
  system.tell(Deposit(accountId, 3))
  system.tell(Withdraw(accountId, 4))
}
