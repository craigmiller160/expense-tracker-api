package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.LastRuleAppliedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.model.LastRuleApplied
import org.springframework.data.jpa.repository.JpaRepository

interface LastRuleAppliedRepository : JpaRepository<LastRuleApplied, TypedId<LastRuleAppliedId>> {
  fun findByUserIdAndTransactionId(
    userId: Long,
    transactionId: TypedId<TransactionId>
  ): LastRuleApplied?
}
