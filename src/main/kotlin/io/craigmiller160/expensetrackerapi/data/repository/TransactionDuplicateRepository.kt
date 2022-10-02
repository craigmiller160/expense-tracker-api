package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionDuplicateId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.model.TransactionDuplicate
import org.springframework.data.jpa.repository.JpaRepository

interface TransactionDuplicateRepository :
  JpaRepository<TransactionDuplicate, TypedId<TransactionDuplicateId>> {
  fun findAllByUserIdAndNewTransactionId(
    userId: Long,
    newTransactionId: TypedId<TransactionId>
  ): List<TransactionDuplicate>
}
