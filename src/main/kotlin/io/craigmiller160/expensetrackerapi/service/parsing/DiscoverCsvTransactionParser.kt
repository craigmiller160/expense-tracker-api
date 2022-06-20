package io.craigmiller160.expensetrackerapi.service.parsing

import arrow.core.Either
import arrow.core.continuations.either
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.function.TryEither
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.springframework.stereotype.Component

@Component
class DiscoverCsvTransactionParser : AbstractCsvTransactionParser() {
  companion object {
    private val DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy")
  }
  override fun getTransaction(
      userId: Long,
      fieldExtractor: FieldExtractor
  ): TryEither<Transaction> =
      either.eager {
        val transactionDate = fieldExtractor(0, "transactionDate").bind()
        val expenseDate = Either.catch { LocalDate.parse(transactionDate, DATE_FORMAT) }.bind()
        val description = fieldExtractor(2, "description").bind()
        val rawAmount = fieldExtractor(3, "amount").bind()
        val amount = Either.catch { BigDecimal(rawAmount) }.bind()
        Transaction(
            userId = userId, expenseDate = expenseDate, description = description, amount = amount)
      }
}
