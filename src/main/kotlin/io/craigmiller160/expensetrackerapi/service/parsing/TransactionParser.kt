package io.craigmiller160.expensetrackerapi.service.parsing

import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.function.TryEither
import java.io.InputStream

interface TransactionParser {
  fun parse(userId: Long, stream: InputStream): TryEither<List<Transaction>>
}
