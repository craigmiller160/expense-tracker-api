package io.craigmiller160.expensetrackerapi.service.parsing

import arrow.core.Either
import arrow.core.sequence
import io.craigmiller160.expensetrackerapi.common.error.InvalidImportException
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.function.TryEither

typealias FieldExtractor = (index: Int, name: String) -> TryEither<String>

abstract class AbstractCsvTransactionParser : TransactionParser {

  override fun parse(transactions: String): TryEither<List<Transaction>> =
      transactions
          .split("\n")
          .asSequence()
          .map { line -> line.split(",").map { it.trim() } }
          .map { prepareFieldExtractor(it) }
          .map { getTransaction(it) }
          .sequence()

  private fun prepareFieldExtractor(fields: List<String>): FieldExtractor = { index, name ->
    Either.catch { fields[index] }
        .mapLeft { InvalidImportException("Missing field $name at CSV row index $index") }
  }

  protected abstract fun getTransaction(fieldExtractor: FieldExtractor): TryEither<Transaction>
}
