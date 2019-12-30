package gr.fpas.bank.be

import akka.actor.typed.{Behavior, ChildFailed}
import akka.actor.typed.scaladsl.Behaviors

/**
 * The single main supervisor of the bank system
 * This is the initial top parent actor that initializes (and monitors) everything.
 *
 * This actor initializes the AccountGroup and the HttpServer actor.
 */
object MainSupervisor {
  // This is a sample of functional style actor
  // see docs: https://doc.akka.io/docs/akka/current/typed/style-guide.html


  /**
   * Actor builder method using scala apply(..)
   */
  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { context =>

    context.log.info("MainSupervisor started")

    // Initialize and watch AccountGroup
    val accountGroup = context.spawn(AccountGroup(), "AccountGroup")

    context.watch(accountGroup)

    // This actor only receives and handles signals from the monitored children
    // That is why it does not have protocol messages
    Behaviors.receiveSignal[Nothing] {

      // This is pattern matching anonymous function scala feature
      // see: https://danielwestheide.com/blog/the-neophytes-guide-to-scala-part-4-pattern-matching-anonymous-functions/
      case (_, ChildFailed(ref, cause)) =>
        context.log.warn("The child actor {} failed because {}", ref , cause.getMessage)
        Behaviors.same
    }
  }
}
