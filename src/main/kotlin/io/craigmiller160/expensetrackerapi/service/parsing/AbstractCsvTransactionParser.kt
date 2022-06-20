package io.craigmiller160.expensetrackerapi.service.parsing

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
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
          .map { includeRecord(it).map { include -> FilteredFieldExtractor(it, include) } }
          .filter { handleEitherFilter(it) }
          .map { either -> either.map { it.fieldExtractor } }
          .mapIndexed { index, either -> mapToTransaction(index, userId, either) }
          .sequence()

  private fun mapToTransaction(
      index: Int,
      userId: Long,
      fieldExtractor: TryEither<FieldExtractor>
  ): TryEither<Transaction> =
      fieldExtractor.flatMap {
        getTransaction(userId, it).mapLeft {
          InvalidImportException("Error parsing CSV record, row ${index + 2}", it)
        }
      }

  private fun handleEitherFilter(either: TryEither<FilteredFieldExtractor>): Boolean =
      either.map { it.include }.getOrElse { true }

  private fun prepareFieldExtractor(fields: Array<String>): FieldExtractor = { index, name ->
    Either.catch { fields[index] }
        .mapLeft { InvalidImportException("Missing field $name at CSV row index $index") }
  }

  protected open fun includeRecord(fieldExtractor: FieldExtractor): TryEither<Boolean> =
      Either.Right(true)

  protected abstract fun getTransaction(
      userId: Long,
      fieldExtractor: FieldExtractor
  ): TryEither<Transaction>

  private data class FilteredFieldExtractor(
      val fieldExtractor: FieldExtractor,
      val include: Boolean
  )
}
