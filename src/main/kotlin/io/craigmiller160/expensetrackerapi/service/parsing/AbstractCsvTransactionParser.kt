package io.craigmiller160.expensetrackerapi.service.parsing

import arrow.core.Either
import arrow.core.sequence
import com.opencsv.CSVReader
import io.craigmiller160.expensetrackerapi.common.error.InvalidImportException
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.function.TryEither
import java.io.InputStream
import java.io.InputStreamReader

typealias FieldExtractor = (index: Int, name: String) -> TryEither<String>

abstract class AbstractCsvTransactionParser : TransactionParser {

  override fun parse(userId: Long, stream: InputStream): TryEither<List<Transaction>> =
      CSVReader(InputStreamReader(stream))
          .readAll()
          .asSequence()
          .drop(1)
          .map { prepareFieldExtractor(it) }
          .mapIndexed { index, fieldExtractor ->
            getTransaction(userId, fieldExtractor).mapLeft {
              InvalidImportException("Error parsing CSV record, row ${index + 2}", it)
            }
          }
          .sequence()

  private fun prepareFieldExtractor(fields: Array<String>): FieldExtractor = { index, name ->
    Either.catch { fields[index] }
        .mapLeft { InvalidImportException("Missing field $name at CSV row index $index") }
  }

  protected abstract fun getTransaction(
      userId: Long,
      fieldExtractor: FieldExtractor
  ): TryEither<Transaction>
}
