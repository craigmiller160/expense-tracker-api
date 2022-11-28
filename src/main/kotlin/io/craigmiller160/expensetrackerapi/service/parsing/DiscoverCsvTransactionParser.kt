package io.craigmiller160.expensetrackerapi.service.parsing

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.sequence
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.function.TryEither
import java.io.InputStream
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.springframework.stereotype.Component

@Component
class DiscoverCsvTransactionParser : TransactionParser {
  companion object {
    private val DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy")
  }

  override fun parse(userId: Long, stream: InputStream): TryEither<List<Transaction>> =
    CsvParser.parse(stream).flatMap(parseRows(userId))

  override fun parseRecord(userId: Long, row: Array<String>): TryEither<Transaction> {
    TODO("Not yet implemented")
  }

  override fun validateImportType(headerRow: Array<String>): TryEither<Unit> {
    TODO("Not yet implemented")
  }

  private fun parseRows(userId: Long): (Sequence<Array<String>>) -> TryEither<List<Transaction>> =
    { rows ->
      rows.map(rowToTransaction(userId)).sequence()
    }

  private fun rowToTransaction(userId: Long): (Array<String>) -> TryEither<Transaction> = { row ->
    Either.catch {
      val transactionDate = row[0]
      val expenseDate = LocalDate.parse(transactionDate, DATE_FORMAT)
      val description = row[2]
      val rawAmount = row[3]
      val amount = BigDecimal(rawAmount).times(BigDecimal("-1"))
      Transaction(
        userId = userId, expenseDate = expenseDate, description = description, amount = amount)
    }
  }
}
