package io.craigmiller160.expensetrackerapi.service.parsing

import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.function.TryEither
import java.io.InputStream
import java.time.format.DateTimeFormatter
import org.springframework.stereotype.Component

@Component
class DiscoverCsvTransactionParser : TransactionParser {
  companion object {
    private val DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy")
  }

  //  val numberOfColumns: Int = 5
  //
  //  fun getTransaction(
  //    userId: Long,
  //    fieldExtractor: FieldExtractor
  //  ): TryEither<Transaction> =
  //    either.eager {
  //      val transactionDate = fieldExtractor(0, "transactionDate").bind()
  //      val expenseDate = Either.catch { LocalDate.parse(transactionDate, DATE_FORMAT) }.bind()
  //      val description = fieldExtractor(2, "description").bind()
  //      val rawAmount = fieldExtractor(3, "amount").bind()
  //      val amount = Either.catch { BigDecimal(rawAmount) }.bind()
  //      Transaction(
  //        userId = userId, expenseDate = expenseDate, description = description, amount = amount)
  //    }

  override fun parse(userId: Long, stream: InputStream): TryEither<List<Transaction>> {
    TODO("Not yet implemented")
  }
}
