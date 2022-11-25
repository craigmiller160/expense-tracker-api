package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.model.TransactionView
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TransactionViewRepository : JpaRepository<TransactionView, TypedId<TransactionId>> {
  fun findByUidAndUserId(id: TypedId<TransactionId>, userId: Long): TransactionView?

  fun findAllByUidInAndUserId(
    transactionIds: List<TypedId<TransactionId>>,
    userId: Long
  ): List<TransactionView>

  @Query(
    """
    SELECT t1
    FROM TransactionView t1
    WHERE t1.contentHash IN (
        SELECT t2.contentHash
        FROM TransactionView t2
        WHERE t2.uid = :transactionId
        AND t2.userId = :userId
    )
    AND t1.uid <> :transactionId
    AND t1.userId = :userId
  """)
  fun findAllDuplicates(
    @Param("transactionId") transactionId: TypedId<TransactionId>,
    @Param("userId") userId: Long,
    page: Pageable
  ): Page<TransactionView>
}
