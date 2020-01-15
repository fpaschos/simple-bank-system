package gr.fpas.bank.be

import java.time.ZonedDateTime

import akka.NotUsed
import akka.actor.ActorSystem
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import gr.fpas.bank.be.AccountHolder.{AccountBalance, Deposit, Deposited, Event, Withdrawed}

import scala.concurrent.Future

object AccountHistoryService {
  def apply(system: ActorSystem): AccountHistoryService = new AccountHistoryService(system)

}

class AccountHistoryService(private val system: ActorSystem) {

  implicit val mat = Materializer(system)

  private lazy val readJournal: JdbcReadJournal =
    PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)




  // Details about the implementation at https://doc.akka.io/docs/akka/current/persistence-query.html
  def queryAccountHistory(accountId: String): Future[Seq[AccountBalance]] = {

    // Build a source of all the current events of the given accountId (=persistenceId)
    val events: Source[Event, NotUsed] =
      readJournal
        .currentEventsByPersistenceId(accountId, 0L, Long.MaxValue)
        .map(_.event)
        .collectType[Event]

    // Build a stateful stream (flow) that reconstructs balances
    // from the events.
    // (Something similar to how the AccountHolder updates its internal state)

    val reconstructBalances: Flow[Event,AccountBalance,NotUsed] =

      // More details about this
      // See https://stackoverflow.com/questions/37902354/akka-streams-state-in-a-flow
      Flow[Event].statefulMapConcat { () =>
        var state = AccountBalance(accountId, 0.0, ZonedDateTime.now())
       _ match {
          case Deposited(amount, created) =>
            state = state.copy(balance = state.balance + amount, updated = created)
            List(state)
          case Withdrawed(amount, created) =>
            state = state.copy(balance = state.balance -amount, updated = created)
            List(state)
        }
      }

    // Run the source via the reconstruct flow to a Sink.seq and collect
    // all the balances history stream in a future ( Cool!!!! )
    events.via(reconstructBalances).runWith(Sink.seq)
  }
}
