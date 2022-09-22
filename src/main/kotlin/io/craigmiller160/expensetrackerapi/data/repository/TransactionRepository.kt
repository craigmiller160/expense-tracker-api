package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsRequest
import java.time.LocalDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TransactionRepository :
  JpaRepository<Transaction, TypedId<TransactionId>>, JpaSpecificationExecutor<Transaction> {
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
        t.version = t.version + 1
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
    SELECT t
    FROM Transaction t
    WHERE (:#{#request.startDate} IS NULL OR :#{#request.startDate} >= t.expenseDate)
    AND (:#{#request.endDate} IS NULL OR :#{#request.endDate} <= t.expenseDate)
    AND (:#{#request.isConfirmed} IS NULL OR :#{#request.isConfirmed} = t.confirmed)
    AND (:#{#request.isDuplicate} IS NULL OR :#{#request.isDuplicate} = t.duplicate)
    AND (:categories IS NULL OR t.categoryId IN (:categories))
    AND CASE
        WHEN (:#{#request.isCategorized} IS NULL) THEN true
        WHEN (:#{#request.isCategorized} = true) THEN (t.categoryId IS NOT NULL)
        ELSE (t.categoryId IS NULL)
    END
  """)
  fun searchForTransaction(
    @Param("request") request: SearchTransactionsRequest,
    @Param("categories") categories: List<TypedId<CategoryId>>?,
    page: Pageable
  ): Page<Transaction>

  @Query(
    """
      UPDATE Transaction t
      SET t.confirmed = :confirmed, 
        t.version = t.version + 1
      WHERE t.id = :transactionId
      AND t.userId = :userId
  """)
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  fun confirmTransaction(
    @Param("transactionId") transactionId: TypedId<TransactionId>,
    @Param("confirmed") confirmed: Boolean,
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
      UPDATE Transaction t
      SET t.categoryId = null, 
        t.version = t.version + 1
      WHERE t.categoryId = (
        SELECT c.id
        FROM Category c
        WHERE c.id = :categoryId
        AND c.userId = :userId
      )
      AND t.userId = :userId
  """)
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  fun removeCategoryFromAllTransactions(
    @Param("userId") userId: Long,
    @Param("categoryId") categoryId: TypedId<CategoryId>
  )

  @Query(
    """
      UPDATE Transaction t
      SET t.categoryId = null
      WHERE t.id = :transactionId
      AND t.userId = :userId
  """)
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  fun removeTransactionCategory(
    @Param("transactionId") transactionId: TypedId<TransactionId>,
    @Param("userId") userId: Long
  )

  @Query(
    """
      SELECT COUNT(t)
      FROM Transaction t
      WHERE t.userId = :userId
      AND t.confirmed = false
  """)
  fun countAllUnconfirmed(@Param("userId") userId: Long): Long

  @Query(
    """
        SELECT MIN(t.expenseDate)
        FROM Transaction t
        WHERE t.userId = :userId
        AND t.confirmed = false
    """)
  fun getOldestUnconfirmedDate(@Param("userId") userId: Long): LocalDate?

  @Query(
    """
      SELECT COUNT(t)
      FROM Transaction t
      WHERE t.userId = :userId
      AND t.categoryId IS NULL
  """)
  fun countAllUncategorized(@Param("userId") userId: Long): Long

  @Query(
    """
      SELECT MIN(t.expenseDate)
      FROM Transaction t
      WHERE t.userId = :userId
      AND t.categoryId IS NULL
  """)
  fun getOldestUncategorizedDate(@Param("userId") userId: Long): LocalDate?

  @Query(
    """
      SELECT COUNT(t)
      FROM Transaction t
      WHERE t.userId = :userId
      AND t.duplicate = true
  """)
  fun countAllDuplicates(@Param("userId") userId: Long): Long

  @Query(
    """
      SELECT MIN(t.expenseDate)
      FROM Transaction t
      WHERE t.userId = :userId
      AND t.duplicate = true
  """)
  fun getOldestDuplicate(@Param("userId") userId: Long): LocalDate?
}
