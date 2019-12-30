package gr.fpas.bank.be

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import akka.{Done, actor => classic}
import akka.http.scaladsl.Http
import akka.actor.typed.scaladsl.adapter._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * Actor that initializes an Http server
 */
object HttpServer {


  // This example is actor is inspired from
  // https://github.com/chbatey/akka-http-typed/blob/master/src/main/scala/info/batey/QuickstartServer.scala

  def apply(accountGroup: ActorRef[AccountGroup.Command]) = Behaviors.setup[Done] { context =>

    context.log.info(s"Http actor  started")

    // Needed for Http server and Futures

    // Akka http uses the classic actor system api
    implicit val untypedSystem: classic.ActorSystem = context.system.toClassic
    implicit val ec: ExecutionContext = context.system.executionContext


    val api = AccountApi(accountGroup, context.system)
    val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(api.routes, "0.0.0.0", 8080)

    serverBinding.onComplete { // Another pattern matching anonymous function
      case Success(bound) =>
        context.log.info(s"Server online at http://${bound.localAddress}")
      case Failure(ex) =>
        context.log.error(s"Server could not start", ex)
    }

    // Return a behavior that terminates the actor if the serverBinding completes
    // that is the akka Http server cannot bind a tcp endpoint with failure.
    Behaviors.receiveMessage {
      case Done =>
        Behaviors.stopped
    }
  }
}
