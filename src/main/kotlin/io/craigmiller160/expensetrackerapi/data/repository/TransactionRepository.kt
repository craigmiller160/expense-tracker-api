package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import java.time.LocalDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

  @Query(
      """
      DELETE FROM Transaction t
      WHERE t.id IN (:transactionIds)
      AND t.userId = :userId
  """)
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  fun deleteTransactions(
      @Param("transactionIds") transactionIds: Set<TypedId<TransactionId>>,
      @Param("userId") userId: Long
  )

  @Query(
      """
      SELECT t.*
      FROM Transaction t
      WHERE (:startDate IS NULL OR t.expenseDate >= :startDate)
      AND (:endDate IS NULL OR t.expenseDate <= :endDate)
      AND (:confirmed IS NULL OR t.confirmed = :confirmed)
  """)
  fun searchTransactions(
      @Param("startDate") startDate: LocalDate?,
      @Param("endDate") endDate: LocalDate?,
      @Param("confirmed") confirmed: Boolean?,
      page: Pageable
  ): Page<Transaction>
}
