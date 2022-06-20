package io.craigmiller160.expensetrackerapi.service.parsing

import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.function.TryEither

class DiscoverCsvTransactionParser : TransactionParser {
  override fun parse(transactions: String): TryEither<List<Transaction>> {
    TODO("Not yet implemented")
  }
}
