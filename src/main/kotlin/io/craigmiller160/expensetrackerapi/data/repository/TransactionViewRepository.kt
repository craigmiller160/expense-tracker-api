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
  fun findByIdAndUserId(id: TypedId<TransactionId>, userId: Long): TransactionView?

  // TODO need to filter by user id
  fun findAllByIdIn(transactionIds: List<TypedId<TransactionId>>): List<TransactionView>

  // TODO need to filter by user id
  @Query(
    """
    SELECT t1
    FROM TransactionView t1
    WHERE t1.contentHash IN (
        SELECT t2.contentHash
        FROM TransactionView t2
        WHERE t2.id = :transactionId
    )
    AND t1.id <> :transactionId
  """)
  fun findAllDuplicates(
    @Param("transactionId") transactionId: TypedId<TransactionId>,
    page: Pageable
  ): Page<TransactionView>
}
