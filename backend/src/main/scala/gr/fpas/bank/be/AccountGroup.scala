package gr.fpas.bank.be

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import gr.fpas.bank.be.AccountGroup.{AccountsList, AvailableAccount, Command, RequestAccount, RequestAccounts}

object AccountGroup {

  sealed trait Command

  final case class RequestAccount(accountId: String, replyTo: ActorRef[Response]) extends Command

  final case class RequestAccounts(replyTo: ActorRef[Response]) extends Command

  trait Response

  final case class AvailableAccount(accountId: String, account: ActorRef[AccountHolder.Command]) extends Response

  final case class AccountsList(accounts: Seq[String]) extends Response

  /**
   * Actor builder method (something like a constructor)
   */
  def create() : Behavior[Command] = Behaviors.setup(context => new AccountGroup(context))

}

/**
 * Implementation of an actor that holds account state.
 * There is ONE ACTOR PER ACCOUNT discriminated by a specific accountId.
 *
 * This is an example of actor class implementation.
 *
 * @param context the actor context
 */
class AccountGroup(context: ActorContext[Command])
  extends AbstractBehavior[Command](context) {

  context.log.info("Account group started")

  private var accounts: Map[String, ActorRef[AccountHolder.Command]] = Map.empty

  override def onMessage(msg: Command): Behavior[Command] =

  // Using advanced pattern matching
    msg match {
      case RequestAccount(accountId, replyTo) =>
        val account = getOrCreateAccount(accountId) // Create or find an existing account by accountId
        replyTo ! AvailableAccount(accountId, account) // Respond with the account found
        Behaviors.same // Keep the same behavior

      case RequestAccounts(replyTo) =>
        replyTo ! AccountsList(accounts.keys.toList) // Respond with the available accounts (online)
        Behaviors.same // Keep the same behavior
    }

  /**
   * Creates a new account actor by accountId or retrieves an existing one from the internal state (in memory cache)
   */
  private def getOrCreateAccount(accountId: String): ActorRef[AccountHolder.Command] = {

    // Using advanced pattern matching
    accounts.get(accountId) match {
      case Some(account) =>
        account

      case None =>
        val newAccount = context.spawn(AccountHolder.create(accountId), accountId) // NOTE!!! the creation of a child actor using spawn
        accounts += accountId -> newAccount  // Adds a key value pair to the map ~ (accountId, newAccount) NOTE!!! Map is IMMUTABLE data structure
        newAccount
    }
  }

}
