package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.LastRuleAppliedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.model.LastRuleApplied
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying

interface LastRuleAppliedRepository :
  JpaRepository<LastRuleApplied, TypedId<LastRuleAppliedId>>, LastRuleAppliedRepositoryCustom {
  fun findByUserIdAndTransactionId(
    userId: Long,
    transactionId: TypedId<TransactionId>
  ): LastRuleApplied?

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  fun deleteAllByUserIdAndTransactionIdIn(
    userId: Long,
    transactionIds: Collection<TypedId<TransactionId>>
  )
}
