package io.craigmiller160.expensetrackerapi.service.parsing

import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.function.flatMapCatch
import org.springframework.stereotype.Component

@Component
class ChaseCsvTransactionParser : AbstractCsvTransactionParser() {
  override fun getTransaction(
      userId: Long,
      fieldExtractor: FieldExtractor
  ): TryEither<Transaction> {
    TODO("Not yet implemented")
  }

  /** Include only negative amounts to prevent adding deposits as expenses */
  override fun includeRecord(fieldExtractor: FieldExtractor): TryEither<Boolean> =
      fieldExtractor(3, "amount").flatMapCatch { amount -> amount.toDouble() < 0 }
}
