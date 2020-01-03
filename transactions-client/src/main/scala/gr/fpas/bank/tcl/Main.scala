package gr.fpas.bank.tcl

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import gr.fpas.bank.tcl.HttpClient.{mkRandomCommands, postCommand}
import akka.{Done, actor => classic}

import scala.concurrent.duration._

object Main extends App {
  // Create an infinite stream that posts random commands every 2 seconds

  implicit val system = classic.ActorSystem()
  implicit val ec = system.dispatcher
  implicit lazy val mat = Materializer(system)

  val stream = Source.tick(0.second, 2.seconds, ())
    .mapConcat {
      _ => mkRandomCommands(500, 500)
    }
    .log("cmd")
    .mapAsync(parallelism = 30) { cmd =>
      postCommand(cmd)
    }
    .runWith(Sink.ignore)


  stream.recover { error =>
    println(s"Stream error: ${error.getMessage}")
    Done
  }.map { _ =>
    println("Terminating normally")
    system.terminate()
  }
}
