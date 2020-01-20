package gr.fpas.bank.be

import java.time.ZonedDateTime

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

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
  final case class AccountBalance(accountId: String, balance: Double, updated: ZonedDateTime) extends Response

  final case class InsufficientFunds(accountId: String) extends Response


  // The persisted events stored in the event sourcing store
  sealed trait Event extends CborSerialized

  final case class Created(accountId: String, balance: Double, created: ZonedDateTime) extends Event

  final case class Deposited(accountId: String,  balance: Double, amount: Double,created: ZonedDateTime) extends Event

  final case class Withdrawed(accountId: String, balance: Double, amount: Double,  created: ZonedDateTime) extends Event



  // State

  sealed trait Account {
    val balance: Double
    val updated: ZonedDateTime // Last update time

    def commandHandler(cmd: Command): Effect[Event, Account]

    def eventHandler(evt: Event): Account
  }

  final case class EmptyAccount(accountId: String) extends Account {
    override val balance: Double = 0.0
    override val updated: ZonedDateTime = ZonedDateTime.now()

    override def commandHandler(cmd: Command): Effect[Event, Account] = cmd match {
      case Deposit(accountId, amount, replyTo) =>

        // The first time that we deposit in an empty account it "activates" and
        // also persists a created event with the same timestamp
        val now = ZonedDateTime.now()
        val firstDepositEvents = Seq(
          Created(accountId, balance, now),
          Deposited(accountId, balance, amount, now)
        )

        Effect.persist(firstDepositEvents)
          .thenReply(replyTo) { st => AccountBalance(accountId, st.balance, updated = st.updated) }

      case Withdraw(accountId, _, replyTo) =>
        Effect.none
          .thenReply(replyTo) { _ => InsufficientFunds(accountId) }

      case GetBalance(accountId, replyTo) =>
        Effect.none
          .thenReply(replyTo) { st => AccountBalance(accountId, st.balance, updated = st.updated) }
    }

    override def eventHandler(evt: Event): Account = evt match {
      case Created(accountId, balance, created) => ActiveAccount(accountId, balance, created)
      case Deposited(accountId, _, amount, created) => ActiveAccount(accountId, amount , created)
      case _ => this // Ignore withdraw events on this state
    }
  }

  final case class ActiveAccount(accountId: String, balance: Double, updated: ZonedDateTime) extends Account {

    private def update(evt: Deposited): ActiveAccount =
      copy(balance = balance + evt.amount, updated = evt.created)


    private def update(evt: Withdrawed): ActiveAccount =
      copy(balance = balance - evt.amount, updated = evt.created)

    private def canWithdraw(amount: Double): Boolean = balance >= amount


    override def commandHandler(cmd: Command): Effect[Event, Account] = cmd match {

      case Deposit(accountId, amount, replyTo) =>
        Effect.persist(Deposited(accountId, balance, amount, ZonedDateTime.now()))
          .thenReply(replyTo) { st => AccountBalance(accountId, st.balance, st.updated) }

      case Withdraw(accountId, amount, replyTo) =>
        if (canWithdraw(amount)) {
          Effect.persist(Withdrawed(accountId, balance, amount, ZonedDateTime.now()))
            .thenReply(replyTo) { st => AccountBalance(accountId, st.balance, st.updated) }
        } else {
          Effect.none
            .thenReply(replyTo) { _ => InsufficientFunds(accountId) }
        }

      case GetBalance(accountId, replyTo) =>
        Effect.none
          .thenReply(replyTo) { st => AccountBalance(accountId, st.balance, st.updated) }
    }

    override def eventHandler(evt: Event): Account = evt match {
      case wd: Withdrawed => update(wd)
      case dp: Deposited => update(dp)
      case _ => this  // Ignore created events on this state
    }
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
    val initial = EmptyAccount(accountId)
    ctx.log.info("AccountHolder {} STARTING")


    EventSourcedBehavior[Command, Event, Account](
      persistenceId = PersistenceId.ofUniqueId(accountId),
      emptyState = initial,
      commandHandler = (state, cmd) => state.commandHandler(cmd),
      eventHandler = (state, evt) => state.eventHandler(evt)
    )
  })
}

