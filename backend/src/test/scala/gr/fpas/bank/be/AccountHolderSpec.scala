package gr.fpas.bank.be

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import gr.fpas.bank.be.AccountHolder.{AccountBalance, Deposit, GetBalance, Response, Withdraw}
import org.scalatest.WordSpecLike

class AccountHolderSpec extends ScalaTestWithActorTestKit with WordSpecLike {

  "AccountHolder actor" should {

    "should initialized with zero balance" in {
      val accountId = "ACC_1"

      val actor = spawn(AccountHolder.create("ACC_1"))

      val probe = createTestProbe[Response]

      actor ! GetBalance(accountId, probe.ref)
      val response = probe.expectMessageType[AccountBalance]

      response.accountId shouldBe accountId
      response.balance shouldBe 0.0
    }

    "should add the deposit amount to the balance" in {
      val accountId = "ACC_1"

      val actor = spawn(AccountHolder.create("ACC_1"))

      val probe = createTestProbe[Response]

      actor ! Deposit(accountId, 11)

      actor ! GetBalance(accountId, probe.ref)
      val response = probe.expectMessageType[AccountBalance]

      response.accountId shouldBe accountId
      response.balance shouldBe 11
    }

    "should subtract the withdraw amount from the balance" in {
      val accountId = "ACC_1"

      val actor = spawn(AccountHolder.create("ACC_1"))

      val probe = createTestProbe[Response]

      actor ! Withdraw(accountId, 1)

      actor ! GetBalance(accountId, probe.ref)
      val response = probe.expectMessageType[AccountBalance]

      response.accountId shouldBe accountId
      response.balance shouldBe -1
    }
  }
}
