package gr.fpas.bank.be

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import gr.fpas.bank.be.AccountGroup._
import org.scalatest.WordSpecLike

class AccountGroupSpec extends ScalaTestWithActorTestKit with WordSpecLike {

  "AccountGroup actor" should {

    "be initialized with empty accounts state" in {
      val actor = spawn(AccountGroup())

      val probe = createTestProbe[Response]

      actor ! RequestAccounts(probe.ref)
      val response = probe.expectMessageType[AccountsList]

      response.accounts.size shouldBe 0
    }

    "be able to create a new account holder" in {
      val accountId = "ACC_1"

      val groupActor = spawn(AccountGroup())

      val probe = createTestProbe[Response]()

      groupActor ! RequestAccount(accountId, probe.ref)

      val msg = probe.expectMessageType[AvailableAccount]
      msg.accountId shouldBe accountId

      // Make sure that the new account holder is working properly
      val accountProbe = createTestProbe[AccountHolder.Response]()
      val accountHolder = msg.account

      accountHolder ! AccountHolder.Deposit(accountId, 23, accountProbe.ref)

      val balance = accountProbe.expectMessageType[AccountHolder.AccountBalance]
      balance.accountId shouldBe accountId
      balance.balance shouldBe 23
    }

    "should return same actor gor same accountId" in {
      val accountId = "ACC_2"

      val groupActor = spawn(AccountGroup())

      val probe = createTestProbe[Response]()

      groupActor ! RequestAccount(accountId, probe.ref)

      val msg1 = probe.expectMessageType[AvailableAccount]
      msg1.accountId shouldBe accountId

      groupActor ! RequestAccount(accountId, probe.ref)

      val msg2 = probe.expectMessageType[AvailableAccount]
      msg2.accountId shouldBe accountId
    }
  }
}
