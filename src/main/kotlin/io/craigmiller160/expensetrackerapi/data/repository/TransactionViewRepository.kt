package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.model.TransactionView
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TransactionViewRepository : JpaRepository<TransactionView, TypedId<TransactionId>> {
  fun findAllByIdIn(transactionIds: List<TypedId<TransactionId>>): List<TransactionView>

  @Query(
    """
    SELECT t
    FROM TransactionView t
    WHERE t.id IN (
        SELECT t
        
    )
  """)
  fun findAllDuplicates(
    transactionId: TypedId<TransactionId>,
    page: Pageable
  ): Page<TransactionView>
}
