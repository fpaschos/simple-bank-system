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


  // Transfers reserves excesses specific messages

  final case class RequestReserve(accountId: String, amount: Double, txId: String, otherAccount: String, replyTo: ActorRef[Response]) extends Command

  final case class RequestExcess(accountId: String, amount: Double, txId: String, otherAccount: String, replyTo: ActorRef[Response]) extends Command

  final case class CompleteTransfer(accountId: String, txId: String) extends Command

  final case class CancelTransfer(accountId: String, txId: String) extends Command


  // Interface of actor outgoing messages (Responses)
  sealed trait Response

  // Respond with the current balance of an account
  final case class AccountBalance(accountId: String, balance: Double, reserves: Double, excesses: Double, updated: ZonedDateTime) extends Response

  final case class InsufficientFunds(accountId: String) extends Response

  final case class InvalidOperation(accountId: String, reason: String) extends Response


  // The persisted events stored in the event sourcing store
  sealed trait Event extends Product with CborSerialized {
    val balance: Double
    val reserves: Double
    val excesses: Double
    val created: ZonedDateTime
  }

  final case class Created(accountId: String,
                           balance: Double,
                           reserves: Double,
                           excesses: Double,
                           created: ZonedDateTime) extends Event

  final case class Deposited(accountId: String,
                             amount: Double,
                             balance: Double,
                             reserves: Double,
                             excesses: Double,
                             created: ZonedDateTime) extends Event

  final case class Withdrawed(accountId: String,
                              amount: Double,
                              balance: Double,
                              reserves: Double,
                              excesses: Double,
                              created: ZonedDateTime) extends Event

  final case class Reserved(accountId: String,
                            txId: String,
                            otherAccount: String,
                            amount: Double,
                            balance: Double,
                            reserves: Double,
                            excesses: Double,
                            created: ZonedDateTime) extends Event

  final case class Excess(accountId: String,
                            txId: String,
                            otherAccount: String,
                            balance: Double,
                            reserves: Double,
                            excesses: Double,
                            amount: Double, created: ZonedDateTime) extends Event

  final case class TransferCanceled(accountId: String,
                          txId: String,
                          otherAccount: String,
                          balance: Double,
                          reserves: Double,
                          excesses: Double,
                          amount: Double,
                          created: ZonedDateTime) extends Event


  final case class TransferCompleted(accountId: String,
                                    txId: String,
                                    otherAccount: String,
                                    balance: Double,
                                    reserves: Double,
                                    excesses: Double,
                                    amount: Double,
                                    created: ZonedDateTime) extends Event



  // State

  sealed trait Account {
    val accountId: String
    val balance: Double
    val reserves: Double
    val excesses: Double
    val updated: ZonedDateTime // Last update time

    def commandHandler(cmd: Command): Effect[Event, Account]

    def eventHandler(evt: Event): Account

    def asAccountBalance: AccountBalance =
      AccountBalance(accountId, balance, reserves, excesses, updated)
  }

  final case class EmptyAccount(accountId: String,
                                balance: Double = 0.0,
                                excesses: Double = 0.0,
                                reserves: Double = 0.0) extends Account {


    override val updated: ZonedDateTime = ZonedDateTime.now()

    override def commandHandler(cmd: Command): Effect[Event, Account] = cmd match {
      case Deposit(accountId, amount, replyTo) =>

        // The first time that we deposit in an empty account it "activates" and
        // also persists a created event with the same timestamp
        val now = ZonedDateTime.now()
        val firstDepositEvents = Seq(
          Created(accountId, balance, reserves, excesses,now),
          Deposited(accountId, amount, balance, reserves, excesses, now)
        )

        Effect.persist(firstDepositEvents)
          .thenReply(replyTo) { _.asAccountBalance }

      case Withdraw(accountId, _, replyTo) =>
        Effect.none
          .thenReply(replyTo) { _ => InsufficientFunds(accountId) }

      case GetBalance(_, replyTo) =>
        Effect.none
          .thenReply(replyTo) { _.asAccountBalance }

      case RequestReserve(_, _, _, _, replyTo) =>
        Effect.none
          .thenReply(replyTo) { _ => InsufficientFunds(accountId) }

      case RequestExcess => ???
    }

    override def eventHandler(evt: Event): Account = evt match {
      case Created(accountId, _, _, _,  created) =>
        ActiveAccount(accountId, balance, reserves, excesses, created)

      case Deposited(accountId, _, amount, created) =>
        ActiveAccount(accountId, amount, reserves, excesses, created)

      case _ => this // Ignore withdraw events on this state
    }
  }

  final case class ActiveAccount(accountId: String,
                                 balance: Double,
                                 reserves: Double,
                                 excesses: Double,
                                 updated: ZonedDateTime) extends Account {

    private def update(evt: Deposited): ActiveAccount =
      copy(balance = balance + evt.amount, updated = evt.created)


    private def update(evt: Withdrawed): ActiveAccount =
      copy(balance = balance - evt.amount, updated = evt.created)

    private def canWithdraw(amount: Double): Boolean = balance >= amount


    override def commandHandler(cmd: Command): Effect[Event, Account] = cmd match {

      case Deposit(accountId, amount, replyTo) =>
        Effect.persist(Deposited(accountId, balance, amount, ZonedDateTime.now()))
          .thenReply(replyTo) { _.asAccountBalance }

      case Withdraw(accountId, amount, replyTo) =>
        if (canWithdraw(amount)) {
          Effect.persist(Withdrawed(accountId, balance, amount, ZonedDateTime.now()))
            .thenReply(replyTo) { _.asAccountBalance }
        } else {
          Effect.none
            .thenReply(replyTo) { _ => InsufficientFunds(accountId) }
        }

      case GetBalance(_, replyTo) =>
        Effect.none
          .thenReply(replyTo) { _.asAccountBalance }
    }

    override def eventHandler(evt: Event): Account = evt match {
      case wd: Withdrawed => update(wd)
      case dp: Deposited => update(dp)
      case _ => this // Ignore created events on this state
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

