package gr.fpas.bank.be

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import gr.fpas.bank.be.AccountHolder.{AccountBalance, Command, Deposit, GetBalance, Withdraw}

object AccountHolder {

  // Protocol ~ messages interface of AccountProcessor

  // Interface of actor incoming messages (Commands)
  sealed trait Command

  // Request to deposit an amount to an  account
  final case class Deposit(accountId: String, amount: Double) extends Command

  // Request to withdraw an amount from an account
  final case class Withdraw(accountId: String, amount: Double) extends Command

  // Request the current balance from an account
  final case class GetBalance(accountId: String, replyTo: ActorRef[Response]) extends Command

  // Interface of actor outgoing messages (Responses)
  sealed trait Response

  // Respond with the current balance of an account
  final case class AccountBalance(accountId: String, balance: Double) extends Response

  /**
   * Actor builder method
   *
   * @param accountId the id of the account
   */
  def apply(accountId: String) : Behavior[Command] = Behaviors.setup(context => new AccountHolder(context, accountId))

}

/**
 * Implementation of an actor that holds bank account state.
 * There is ONE ACTOR PER ACCOUNT discriminated by a specific accountId.
 *
 * This is an example of actor class implementation.
 *
 * @param context the actor context
 * @param accountId the id of the account (internal actor state)
 */
class AccountHolder(context: ActorContext[Command],
                    private val accountId: String) extends AbstractBehavior[Command](context) {

  /**
   * The current account balance initialized with zero value (account is empty)
   *
   * This is internal actor state.
   */
  private var balance = 0.0

  context.log.info("AccountHolder {} STARTED BALANCE {}", accountId, balance)


  // Handle message commands
  // This is essentially a switch
  override def onMessage(msg: Command): Behavior[Command] =
    msg match {
      case cmd: Deposit =>
        balance += cmd.amount  // Change state
        context.log.info("AccountHolder {} DEPOSIT {} BALANCE {}",accountId, cmd.amount, balance)
        Behaviors.same          // Return the same behavior

      case cmd: Withdraw =>
        balance -= cmd.amount  // Change state
        context.log.info("AccountHolder {} WITHDRAW {} BALANCE {}" ,accountId, cmd.amount, balance)
        Behaviors.same          // Return the same behavior

      case cmd: GetBalance =>
        cmd.replyTo ! AccountBalance(accountId, balance) // Respond to the "replyTo" actor with the current balance
        context.log.info("AccountHolder {} BALANCE RESPONSE {}",accountId, balance)
        Behaviors.same          // Return the same behavior
    }
}
