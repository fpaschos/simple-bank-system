package gr.fpas.bank.be

import akka.NotUsed
import akka.actor.ActorSystem
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import gr.fpas.bank.be.AccountHolder.{AccountBalance, Created, Deposited, Excess, Reserved, TransferCancelled, TransferCompleted, Withdrawed}
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
  def query(accountId: String, offset: Long): Future[AccountHistory] = {

    // Details about the implementation at https://doc.akka.io/docs/akka/current/persistence-query.html\
    // Build a source of all the current events of the given accountId (=persistenceId)
    val events: Source[EventEnvelope, NotUsed] =
    readJournal
      .currentEventsByPersistenceId(accountId, offset, Long.MaxValue)
      .collectType[EventEnvelope]

    // Build a stateful stream (flow) that reconstructs balances
    // from the events.
    val reconstructBalances: Flow[EventEnvelope, AccountBalance, NotUsed] =

    // More details about this
    // See https://stackoverflow.com/questions/37902354/akka-streams-state-in-a-flow
    Flow[EventEnvelope]
      .collect {
        _.event match {
          case Created(id, balance, reserves, excesses, created) =>
            AccountBalance(id, balance, reserves, excesses, created)
          case Deposited(id, balance, reserves, excesses, amount, created) =>
            AccountBalance(id, balance + amount, reserves, excesses, created)
          case Withdrawed(id, balance, reserves, excesses, amount, created) =>
            AccountBalance(id, balance - amount, reserves, excesses, created)
          case Reserved(id, _, _, balance, reserves, excesses, amount, created) =>
            AccountBalance(id, balance, reserves + amount, excesses, created)
          case Excess(id, _, _, balance, reserves, excesses, amount, created) =>
            AccountBalance(id, balance, reserves, excesses + amount, created)
//          case TransferCompleted(id, balance, reserves, excesses, amount, created) =>
//            AccountBalance(id, balance - amount, reserves, excesses, created)
//          case TransferCancelled(id, balance, reserves, excesses, amount, created) =>
//            AccountBalance(id, balance - amount, reserves, excesses, created)
        }
      }

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
    } yield {
      //Handle the case where the stream wsas empty and no events where produced
      val finalLastOffset = if (lastOffset == 0) {
        offset
      } else {
        lastOffset
      }

      AccountHistory(accountId, series, series.size, offset, finalLastOffset)
    }
  }
}
