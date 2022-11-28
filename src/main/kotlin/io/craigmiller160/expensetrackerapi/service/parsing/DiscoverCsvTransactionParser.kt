package io.craigmiller160.expensetrackerapi.service.parsing

import arrow.core.Either
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

  override fun parseRecord(userId: Long, row: Array<String>): TryEither<Transaction> =
    Either.catch {
      val transactionDate = row[0]
      val expenseDate = LocalDate.parse(transactionDate, DATE_FORMAT)
      val description = row[2]
      val rawAmount = row[3]
      val amount = BigDecimal(rawAmount).times(BigDecimal("-1"))
      Transaction(
        userId = userId, expenseDate = expenseDate, description = description, amount = amount)
    }

  override fun validateImportType(headerRow: Array<String>): TryEither<Unit> {
    TODO("Not yet implemented")
  }
}
