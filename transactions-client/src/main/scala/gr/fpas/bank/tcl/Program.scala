package gr.fpas.bank.tcl

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import gr.fpas.bank.tcl.HttpClient.{mkRandomCommands, postCommand}
import akka.{Done, actor => classic}

import scala.concurrent.duration._

/**
 * A simple program  that creates an infinite stream that posts multiple random commands every 2 seconds to the backend.
 */
object Program extends App {


  implicit val system = classic.ActorSystem()
  implicit val ec = system.dispatcher
  implicit lazy val mat = Materializer(system)

  val stream = Source.tick(0.second, 2.seconds, ()) // Every 2 seconds
    .mapConcat {    // Generate random account commands (Deposit or Withdraw)
      _ => mkRandomCommands(1000, 500)
    }
    .mapAsync(parallelism = 30) { cmd =>  // For each command send a POST request with max 30 parallel
      postCommand(cmd)
    }
    .runWith(Sink.ignore)


  // In case of error print the cause and terminate the stream
  stream.recover { error =>
    println(s"Stream error: ${error.getMessage}")
    Done
  }.map { _ =>
    println("Terminating")
    system.terminate()
  }
}
