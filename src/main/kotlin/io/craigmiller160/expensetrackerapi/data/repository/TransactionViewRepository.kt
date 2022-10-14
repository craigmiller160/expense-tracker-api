package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.model.TransactionView
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionViewRepository : JpaRepository<TransactionView, TypedId<TransactionId>> {
  fun findAllByIdIn(transactionIds: List<TypedId<TransactionId>>): List<TransactionView>
}
