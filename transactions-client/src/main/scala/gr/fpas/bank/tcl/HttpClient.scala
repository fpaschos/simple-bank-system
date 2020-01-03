package gr.fpas.bank.tcl

import java.util.concurrent.ThreadLocalRandom

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.{Done, actor => classic}

import scala.concurrent.Future

// Some dtos
sealed trait ClientCommand

final case class Deposit(accountId: String, amount: Double) extends ClientCommand

final case class Withdraw(accountId: String, amount: Double) extends ClientCommand


// Some exceptions
final case class ServiceUnexpectedException(status: StatusCode) extends RuntimeException(s"Response status = $status")

/**
 * An HttpClient using akka streams to post random ClientCommand(s) to our backend each 2 seconds
 */
object HttpClient {


  private def mkEntity(body: String): HttpEntity.Strict = HttpEntity(ContentTypes.`application/json`, body)

  private def mkRequest(cmd: ClientCommand): HttpRequest = {

    val endpoint = "http://localhost:9090/account"

    cmd match {
      case Deposit(accountId, amount) =>
        val body = s"""{ "amount": $amount}""" // Creating post body manually for simplicity
        HttpRequest(
          method = HttpMethods.POST,
          uri = s"$endpoint/$accountId/deposit",
          entity = mkEntity(body)
        )
      case Withdraw(accountId, amount) =>
        val body = s"""{ "amount": $amount}""" // Creating post body manually for simplicity
        HttpRequest(
          method = HttpMethods.POST,
          uri = s"$endpoint/$accountId/withdraw",
          entity = mkEntity(body)
        )
    }
  }


  /**
   * Post a ClientCommand using akka Http client to the server using mkRequest
   *
   * @param cmd the command to post
   * @param s   the akka actor system to be used
   * @return a Future[Done] if the request was made successfully an exception otherwise
   * @throws ServiceUnexpectedException
   * @see HttpClient.mkRequest
   */
  def postCommand(cmd: ClientCommand)(implicit s: classic.ActorSystem): Future[Done] = {
    implicit val ec = s.dispatcher
    val settings = ConnectionPoolSettings(s).withMaxRetries(0) // Ignore this line solves am internal caching problem of Http client in case of DNS refuse connection cases (the server is down)

    // Make the request and respond with Done or an exception according to the response
    Http().singleRequest(request = mkRequest(cmd), settings = settings)
      .map {
        case HttpResponse(StatusCodes.OK, _, _, _) =>
          Done
        case HttpResponse(status, _, _, _) =>
          throw ServiceUnexpectedException(status)
      }
  }

  /**
   * Create a random sequence of client commands.
   * The commands may be deposit or withdraw randomly.
   *
   * @param num         the maximum number of commands to create
   * @param maxAccounts the maximum number of accounts.
   * @return a list of ClientCommand(s)
   */
  def mkRandomCommands(num: Int, maxAccounts: Int = 1000): Seq[ClientCommand] =
    (1 to num).iterator.map { _ =>
      val random = ThreadLocalRandom.current()
      val accountId = s"ACC_${random.nextInt(maxAccounts)}" // possible ids size  <=  maxAccounts
      val amount = random.nextInt(10) // Only positive amounts <= 500

      val isDeposit = random.nextBoolean()

      if (isDeposit) {
        Deposit(accountId, amount)
      } else {
        Withdraw(accountId, amount)
      }
    }.toSeq

}
