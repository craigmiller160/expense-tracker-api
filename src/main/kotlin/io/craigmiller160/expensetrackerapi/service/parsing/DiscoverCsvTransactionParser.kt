package io.craigmiller160.expensetrackerapi.service.parsing

import arrow.core.Either
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.function.tryEither
import java.time.format.DateTimeFormatter

class DiscoverCsvTransactionParser : AbstractCsvTransactionParser() {
  companion object {
    private val DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy")
  }
  override fun getTransaction(fieldExtractor: FieldExtractor): TryEither<Transaction> =
      tryEither.eager {
        val transactionDate = fieldExtractor(0, "transactionDate").bind()
        val expenseDate = Either.catch { DATE_FORMAT.parse(transactionDate) }.bind()

        TODO()
      }
}
