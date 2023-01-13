package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.data.model.TransactionView
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TransactionViewRepository : JpaRepository<TransactionView, TypedId<TransactionId>> {
  fun findByUidAndUserId(id: TypedId<TransactionId>, userId: TypedId<UserId>): TransactionView?

  fun findAllByUidInAndUserId(
    transactionIds: List<TypedId<TransactionId>>,
    userId: TypedId<UserId>
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
    ORDER BY t1.updated DESC
  """)
  fun findAllDuplicates(
    @Param("transactionId") transactionId: TypedId<TransactionId>,
    @Param("userId") userId: TypedId<UserId>,
    page: Pageable
  ): Page<TransactionView>
}
