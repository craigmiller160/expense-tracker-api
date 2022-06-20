package io.craigmiller160.expensetrackerapi.service.parsing

import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.function.TryEither
import java.math.BigDecimal
import org.springframework.stereotype.Component

@Component
class ChaseCsvTransactionParser : AbstractCsvTransactionParser() {
  override fun getTransaction(
      userId: Long,
      fieldExtractor: FieldExtractor
  ): TryEither<Transaction> {
    TODO("Not yet implemented")
  }

  /** Because of parsing logic, negative amounts are deposits, not expenses */
  override fun includeTransaction(transaction: Transaction): Boolean =
      transaction.amount >= BigDecimal("0")
}
