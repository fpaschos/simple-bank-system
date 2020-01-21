package gr.fpas.bank.be

import akka.NotUsed
import akka.actor.ActorSystem
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import gr.fpas.bank.be.AccountHolder.Event
import gr.fpas.bank.be.domain.Domain.AccountEventHistory

import scala.concurrent.Future

object AccountEventHistoryService {
  def apply(system: ActorSystem): AccountEventHistoryService = new AccountEventHistoryService(system)
}

class AccountEventHistoryService(private val system: ActorSystem) {

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
  def query(accountId: String, offset: Long): Future[AccountEventHistory] = {

    // Details about the implementation at https://doc.akka.io/docs/akka/current/persistence-query.html\
    // Build a source of all the current events of the given accountId (=persistenceId)
    val events: Source[EventEnvelope, NotUsed] =
    readJournal
      .currentEventsByPersistenceId(accountId, offset, Long.MaxValue)
      .collectType[EventEnvelope]

    // Build a stateful stream (flow) that reconstructs balances
    // from the events.
    // (Something similar to how the AccountHolder updates its internal state)

    val reconstructBalances: Flow[EventEnvelope, Event, NotUsed] =
      Flow[EventEnvelope]
      .map(_.event)
      .collect{ case ev:Event => ev}

    // Run the source via the reconstruct flow to a Sink.seq and collect
    // all the balances history stream in a future ( Cool!!!! )
    val (offsetF, seriesF) = events
      .alsoToMat(Sink.fold(0L)((_, ev) => ev.sequenceNr))(Keep.right)
      .via(reconstructBalances)
      .toMat(Sink.seq)(Keep.both)
      .run()

    for {
      series <- seriesF
      lastOffset <- offsetF
    } yield
      AccountEventHistory(accountId, series, series.size, offset, lastOffset)
  }
}
