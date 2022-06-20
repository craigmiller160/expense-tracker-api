package io.craigmiller160.expensetrackerapi.service.parsing

import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.function.TryEither

interface TransactionParser {
  fun parse(transactions: String): TryEither<List<Transaction>>
}
