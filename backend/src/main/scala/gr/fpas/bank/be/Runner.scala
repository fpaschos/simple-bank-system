package gr.fpas.bank.be

import akka.actor.typed.ActorSystem

/**
 * This is the "main" class called Runner
 */
object Runner extends App {

  // Note the use of Nothing, that means the top level actor of the system does not accept messages.
  val system = ActorSystem[Nothing](MainSupervisor(), "bank-backend-system")
}
