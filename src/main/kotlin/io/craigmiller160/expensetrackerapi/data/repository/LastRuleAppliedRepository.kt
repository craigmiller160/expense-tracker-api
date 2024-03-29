package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.LastRuleAppliedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.data.model.LastRuleApplied
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface LastRuleAppliedRepository :
    JpaRepository<LastRuleApplied, TypedId<LastRuleAppliedId>>, LastRuleAppliedRepositoryCustom {
  fun findByUserIdAndTransactionId(
      userId: TypedId<UserId>,
      transactionId: TypedId<TransactionId>
  ): LastRuleApplied?

  @Query(
      """
    DELETE FROM LastRuleApplied lra
    WHERE lra.userId = :userId
    AND lra.transactionId IN (:transactionIds)
  """)
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  fun deleteAllByUserIdAndTransactionIdIn(
      @Param("userId") userId: TypedId<UserId>,
      @Param("transactionIds") transactionIds: Collection<TypedId<TransactionId>>
  )
}
