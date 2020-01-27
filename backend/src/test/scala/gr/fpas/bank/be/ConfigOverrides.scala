package gr.fpas.bank.be

import java.util.UUID

object ConfigOverrides {

  // For details on how to test persistent actors see
  // https://doc.akka.io/docs/akka/current/typed/persistence-testing.html

  def inMemoryPersistence: String =
    s"""
       | akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
       | akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
       | akka.persistence.snapshot-store.local-dir = "target/snapshot-${UUID.randomUUID().toString}"
       |""".stripMargin
}
