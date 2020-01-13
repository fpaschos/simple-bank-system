package gr.fpas.bank.be

import java.time.ZonedDateTime

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
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


  // The persisted events stored in the event sourcing store
  sealed trait Event extends CborSerialized

  final case class Deposited(amount: Double, created: ZonedDateTime) extends Event
  final case class Withdrawed(amount: Double, created: ZonedDateTime) extends Event




  // State

  sealed trait Account {
    val balance: Double

    def commandHandler(cmd: Command): Effect[Event, Account]

    def eventHandler(evt: Event): Account
  }

  final case class EmptyAccount(accountId: String) extends Account {
    override val balance: Double = 0.0

    override def commandHandler(cmd: Command): Effect[Event, Account] = cmd match {
      case Deposit(accountId, amount, replyTo) =>
        Effect.persist(Deposited(amount, ZonedDateTime.now()))
          .thenReply(replyTo){ st =>  AccountBalance(accountId, st.balance)}

      case Withdraw(accountId, _, replyTo) =>
        Effect.none
          .thenReply(replyTo){ _=>  InsufficientFunds(accountId)}

      case GetBalance(accountId, replyTo) =>
        Effect.none
          .thenReply(replyTo){st => AccountBalance(accountId, st.balance)}
    }

    override def eventHandler(evt: Event): Account = evt match {
      case Deposited(amount, created) => ActiveAccount(accountId, amount, created)
      case _ => this
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
        Effect.persist(Deposited(amount, ZonedDateTime.now()))
          .thenReply(replyTo){ st =>  AccountBalance(accountId, st.balance)}

      case Withdraw(accountId, amount, replyTo) =>

        if(canWithdraw(amount)) {
          Effect.persist(Withdrawed(amount, ZonedDateTime.now()))
            .thenReply(replyTo){ st =>  AccountBalance(accountId, st.balance)}
        } else {
          Effect.none
            .thenReply(replyTo){ _=>  InsufficientFunds(accountId)}
        }

      case GetBalance(accountId, replyTo) =>
        Effect.none
          .thenReply(replyTo){st => AccountBalance(accountId, st.balance)}
    }

    override def eventHandler(evt: Event): Account = evt match {
      case wd:Withdrawed => update(wd)
      case dp:Deposited => update(dp)
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
    ctx.log.info("AccountHolder {} STARTING BALANCE 0.0", initial.balance)


    EventSourcedBehavior[Command, Event, Account](
      persistenceId = PersistenceId.ofUniqueId(accountId),
      emptyState = initial,
      commandHandler = (state, cmd) => state.commandHandler(cmd),
      eventHandler = (state, evt) => state.eventHandler(evt)
    )

  })



  // Define the single running state of the account
  private def running(acc: Account): Behavior[Command] = ???
//    Behaviors.receiveMessage[Command] {
//      case cmd: Deposit =>
//        val newAcc = acc.update(cmd)
//        cmd.replyTo ! AccountBalance(acc.accountId, newAcc.balance)
//        running(newAcc) // Respond with the current balance
//
//      case cmd: Withdraw =>
//        if(acc.canWithdraw(cmd.amount)) {
//          val newAcc = acc.update(cmd)
//          cmd.replyTo ! AccountBalance(acc.accountId, newAcc.balance)
//          running(newAcc) // Respond with the current balance
//
//        } else {
//          cmd.replyTo ! InsufficientFunds(acc.accountId) // Respond with the current balance
//          running(acc) // Respond with the current balance
//        }
//
//      case cmd: GetBalance =>
//        cmd.replyTo ! AccountBalance(acc.accountId, acc.balance) // Respond to the "replyTo" actor with the current balance
//        running(acc)
//    }
}

