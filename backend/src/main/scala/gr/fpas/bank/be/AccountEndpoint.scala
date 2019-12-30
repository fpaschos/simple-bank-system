package gr.fpas.bank.be

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

object AccountEndpoint {
  lazy val routes: Route = pathPrefix("account") {
    concat(
      deposit(),
      withdraw(),
      balance()
    )
        //        withdraw(accountId)

  }

  def deposit(): Route = path(Segment / "deposit") { accountId =>
    post {
      complete(StatusCodes.OK, s"deposit to $accountId")
    }
  }

  def withdraw(): Route = path(Segment / "withdraw") { accountId =>
    post {
      complete(StatusCodes.OK, s"withdraw from $accountId")
    }
  }

  def balance(): Route = path(Segment) { accountId =>
      get {
        complete(StatusCodes.OK, s"getting balance of $accountId")
      }
    }
}
