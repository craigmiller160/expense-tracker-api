package io.craigmiller160.expensetrackerapi.data.repository.impl

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.projection.LastRuleAppliedForTransaction
import io.craigmiller160.expensetrackerapi.data.repository.LastRuleAppliedRepositoryCustom
import org.springframework.stereotype.Repository

@Repository
class LastRuleAppliedRepositoryCustomImpl : LastRuleAppliedRepositoryCustom {
  override fun getLastRuleDetailsForTransaction(
    userId: Long,
    transactionId: TypedId<TransactionId>
  ): LastRuleAppliedForTransaction? {
    TODO("Not yet implemented")
  }
}
