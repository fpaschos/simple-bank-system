package gr.fpas.bank.be

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import gr.fpas.bank.be.AccountHolder.{AccountBalance, Command, Deposit, GetBalance, Withdraw}

object AccountHolder {

  // Protocol ~ messages interface of AccountProcessor

  // Interface of actor incoming messages (Commands)
  sealed trait Command

  // Request to deposit an amount to an  account
  final case class Deposit(accountId: String, amount: Double, replyTo: ActorRef[Response]) extends Command

  // Request to withdraw an amount from an account
  final case class Withdraw(accountId: String, amount: Double, replyTo: ActorRef[Response]) extends Command

  // Request the current balance from an account
  final case class GetBalance(accountId: String, replyTo: ActorRef[Response]) extends Command

  // Interface of actor outgoing messages (Responses)
  sealed trait Response

  // Respond with the current balance of an account
  final case class AccountBalance(accountId: String, balance: Double) extends Response

  final case class  InsufficientFunds(accountId: String) extends Response


  // State
  final case class Account(accountId: String, balance: Double) {

    def update(cmd: Deposit): Account =
      copy(balance = balance + cmd.amount)


    def update(cmd: Withdraw): Account =
      copy(balance = balance - cmd.amount)

    def canWithdraw(amount: Double): Boolean = balance >= amount
  }

  /**
   * Implementation of an actor that holds bank account state.
   * There is ONE ACTOR PER ACCOUNT discriminated by a specific accountId.
   *
   * This is an example of functional actor implementation
   *
   * @param accountId the id of the account (internal actor state)
   */
  def apply(accountId: String): Behavior[Command] = Behaviors.setup(ctx => {
    // Define the initial state
    val initial = Account(accountId, 0.0)
    ctx.log.info("AccountHolder {} STARTED BALANCE {}", initial.accountId, initial.balance)
    running(initial)
  })

  // Define the single running state of the account
  private def running(acc: Account): Behavior[Command] =
    Behaviors.receiveMessage[Command] {
      case cmd: Deposit =>
        val newAcc = acc.update(cmd)
        cmd.replyTo ! AccountBalance(acc.accountId, newAcc.balance)
        running(newAcc) // Respond with the current balance

      case cmd: Withdraw =>
        if(acc.canWithdraw(cmd.amount)) {
          val newAcc = acc.update(cmd)
          cmd.replyTo ! AccountBalance(acc.accountId, newAcc.balance)
          running(newAcc) // Respond with the current balance

        } else {
          cmd.replyTo ! InsufficientFunds(acc.accountId) // Respond with the current balance
          running(acc) // Respond with the current balance
        }

``      case cmd: GetBalance =>
        cmd.replyTo ! AccountBalance(acc.accountId, acc.balance) // Respond to the "replyTo" actor with the current balance
        running(acc)
    }
}

