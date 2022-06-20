package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TransactionRepository : JpaRepository<Transaction, TypedId<TransactionId>> {
  fun findAllByOrderByExpenseDateAsc(): List<Transaction>

  @Query(
      """
    UPDATE Transaction t
    SET t.categoryId = (
        SELECT c.id
        FROM Category c
        WHERE c.id = :categoryId 
        AND c.userId = :userId
    ), 
        t.updated = current_timestamp,
        t.version = t.version + 1,
        t.confirmed = true
    WHERE t.id = :transactionId
    AND t.userId = :userId
  """)
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  fun setTransactionCategory(
      @Param("transactionId") transactionId: TypedId<TransactionId>,
      @Param("categoryId") categoryId: TypedId<CategoryId>,
      @Param("userId") userId: Long
  )
}
