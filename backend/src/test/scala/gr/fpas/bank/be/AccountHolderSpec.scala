package gr.fpas.bank.be

import akka.actor.testkit.typed.scaladsl.LogCapturing
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import gr.fpas.bank.be.AccountHolder.{AccountBalance, Deposit, GetBalance, InsufficientFunds, Response, Withdraw}
import org.scalatest.WordSpecLike


class AccountHolderSpec extends ScalaTestWithActorTestKit(ConfigOverrides.inMemoryPersistence) with WordSpecLike with LogCapturing {

  "AccountHolder actor" should {

    "should initialized with zero balance" in {
      val accountId = "ACC_1"

      val actor = spawn(AccountHolder(accountId))

      val probe = createTestProbe[Response]

      actor ! GetBalance(accountId, probe.ref)
      val response = probe.expectMessageType[AccountBalance]

      response.accountId shouldBe accountId
      response.balance shouldBe 0.0
    }

    "should add the deposit amount to the balance" in {
      val accountId = "ACC_2"

      val actor = spawn(AccountHolder(accountId))

      val probe = createTestProbe[Response]

      actor ! Deposit(accountId, 11, probe.ref)
      probe.receiveMessage()

      actor ! GetBalance(accountId, probe.ref)
      val response = probe.expectMessageType[AccountBalance]

      response.accountId shouldBe accountId
      response.balance shouldBe 11
    }

    "should subtract the withdraw amount from the balance" in {
      val accountId = "ACC_3"

      val actor = spawn(AccountHolder(accountId))

      val probe = createTestProbe[Response]

      actor ! Deposit(accountId, 10, probe.ref)
      probe.receiveMessage()

      actor ! Withdraw(accountId, 1, probe.ref)
      probe.expectMessageType[AccountBalance]

      actor ! GetBalance(accountId, probe.ref)
      val response = probe.expectMessageType[AccountBalance]

      response.accountId shouldBe accountId
      response.balance shouldBe 9.0
    }

    "should respond with  InsufficientFunds message when the balance does not suffice for withdraw" in {
      val accountId = "ACC_4"

      val actor = spawn(AccountHolder(accountId))

      val probe = createTestProbe[Response]

      actor ! Deposit(accountId, 1, probe.ref)
      probe.receiveMessage()

      actor ! Withdraw(accountId, 9.9, probe.ref)
      probe.expectMessageType[InsufficientFunds]

      actor ! GetBalance(accountId, probe.ref)
      val response = probe.expectMessageType[AccountBalance]

      response.accountId shouldBe accountId
      response.balance shouldBe 1
    }
  }
}
