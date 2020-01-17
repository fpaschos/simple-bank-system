package gr.fpas.bank.be.domain

import gr.fpas.bank.be.AccountHolder.AccountBalance

object Domain {

  /** Response dto of AccountHistoryService.queryAccountHistory */
  case class AccountHistory(accountId: String,
                            series: Seq[AccountBalance],
                            size: Long,
                            startOffset: Long,
                            endOffset: Long)

}
