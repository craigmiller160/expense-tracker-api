package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TransactionRepository :
  JpaRepository<Transaction, TypedId<TransactionId>>,
  JpaSpecificationExecutor<Transaction>,
  TransactionRepositoryCustom {
  fun findAllByOrderByExpenseDateAscDescriptionAsc(): List<Transaction>

  fun findAllByUserIdAndContentHashInOrderByCreated(
    userId: Long,
    contentHash: Collection<String>
  ): List<Transaction>

  fun findByIdAndUserId(id: TypedId<TransactionId>, userId: Long): Transaction?

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
    UPDATE Transaction t
    SET t.markNotDuplicateNano = :nano
    WHERE t.id = :transactionId
  """)
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  fun markNotDuplicate(
    @Param("nano") nano: Long,
    @Param("transactionId") transactionId: TypedId<TransactionId>
  )
}
