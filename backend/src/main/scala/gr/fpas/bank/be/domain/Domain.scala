package gr.fpas.bank.be.domain

import gr.fpas.bank.be.AccountHolder.{AccountBalance, Event}

object Domain {

  /** Response dto of AccountHistoryService.query */
  case class AccountHistory(accountId: String,
                            series: Seq[AccountBalance],
                            size: Long,
                            startOffset: Long,
                            endOffset: Long)


  /** Response dto of AccountEventHistoryService.query */
  case class AccountEventHistory(accountId: String,
                            series: Seq[Event],
                            size: Long,
                            startOffset: Long,
                            endOffset: Long)

}
