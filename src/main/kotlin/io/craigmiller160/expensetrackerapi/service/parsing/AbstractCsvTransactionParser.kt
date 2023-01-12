package io.craigmiller160.expensetrackerapi.service.parsing

import arrow.core.flatMap
import arrow.core.sequence
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.function.flatMapKeepRight
import java.io.InputStream

abstract class AbstractCsvTransactionParser : TransactionParser {
  override fun parse(userId: TypedId<UserId>, stream: InputStream): TryEither<List<Transaction>> =
    CsvParser.parse(stream)
      .flatMapKeepRight { data -> validateImportType(data.header) }
      .flatMap { data -> data.records.map { parseRecord(userId, it) }.sequence() }

  abstract fun parseRecord(userId: TypedId<UserId>, row: Array<String>): TryEither<Transaction>

  abstract fun validateImportType(headerRow: Array<String>): TryEither<Unit>
}
