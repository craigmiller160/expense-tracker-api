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
class ChaseCsvTransactionParser : AbstractCsvTransactionParser() {
  companion object {
    private val DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy")
  }

  override val numberOfColumns: Int = 8

  override fun getTransaction(
      userId: Long,
      fieldExtractor: FieldExtractor
  ): TryEither<Transaction> =
      either.eager {
        val rawDate = fieldExtractor(1, "postingDate").bind()
        val expenseDate = Either.catch { LocalDate.parse(rawDate, DATE_FORMAT) }.bind()
        val description = fieldExtractor(2, "description").bind()
        val rawAmount = fieldExtractor(3, "amount").bind()
        val amount = Either.catch { BigDecimal(rawAmount).times(BigDecimal("-1")) }.bind()
        Transaction(
            userId = userId, expenseDate = expenseDate, description = description, amount = amount)
      }

  /** Because of parsing logic, negative amounts are deposits, not expenses */
  override fun includeTransaction(transaction: Transaction): Boolean =
      transaction.amount >= BigDecimal("0")
}
