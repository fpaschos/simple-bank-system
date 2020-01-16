package gr.fpas.bank.be

import java.time.ZonedDateTime

import akka.NotUsed
import akka.actor.ActorSystem
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import gr.fpas.bank.be.AccountHolder.{AccountBalance, Deposit, Deposited, Event, Withdrawed}
import gr.fpas.bank.be.domain.Domain.AccountHistory

import scala.concurrent.Future

object AccountHistoryService {
  def apply(system: ActorSystem): AccountHistoryService = new AccountHistoryService(system)

}

class AccountHistoryService(private val system: ActorSystem) {

  implicit val mat = Materializer(system)
  implicit val ec = system.dispatcher

  private lazy val readJournal: JdbcReadJournal =
    PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)


  /**
   * Reads the events from the journal for a given accountId from the given offset and converts them to a sequence of AccountBalance
   *
   * @param accountId of the account that we are interested
   * @param offset    the starting offset to begin reading from the journal
   * @return Future[Acc]
   */
  def queryAccountHistory(accountId: String, offset: Long): Future[AccountHistory] = {

    // Details about the implementation at https://doc.akka.io/docs/akka/current/persistence-query.html\
    // Build a source of all the current events of the given accountId (=persistenceId)
    val events: Source[EventEnvelope, NotUsed] =
    readJournal
      .currentEventsByPersistenceId(accountId, offset, Long.MaxValue)
      .collectType[EventEnvelope]

    // Build a stateful stream (flow) that reconstructs balances
    // from the events.
    // (Something similar to how the AccountHolder updates its internal state)

    val reconstructBalances: Flow[EventEnvelope, AccountBalance, Future[Long]] =

    // More details about this
    // See https://stackoverflow.com/questions/37902354/akka-streams-state-in-a-flow
      Flow[EventEnvelope]
        .alsoToMat(Sink.fold(0L)((_, env) => env.sequenceNr))(Keep.right)
        .statefulMapConcat { () =>
          var state = AccountBalance(accountId, 0.0, ZonedDateTime.now())


          envelop =>
            envelop.event match {
              case Deposited(amount, created) =>
                state = state.copy(balance = state.balance + amount, updated = created)
                List(state)
              case Withdrawed(amount, created) =>
                state = state.copy(balance = state.balance - amount, updated = created)
                List(state)
            }
        }

    // TODO extract offset
    //
    //    val lastOffset: Source[AccountBalance, Future[Long]] = events.viaMat(reconstructBalances)(Keep.right)
    //
    //    val series = events
    //      .via(reconstructBalances).viaMat(Sink.seq)(Keep.right)
    //
    //    lastOffset.toMat(series).

    // Run the source via the reconstruct flow to a Sink.seq and collect
    // all the balances history stream in a future ( Cool!!!! )

    events
      .via(reconstructBalances)
      .runWith(Sink.seq)
      .map { series =>
        AccountHistory(accountId, series, series.size, offset, offset)
      }
  }
}
