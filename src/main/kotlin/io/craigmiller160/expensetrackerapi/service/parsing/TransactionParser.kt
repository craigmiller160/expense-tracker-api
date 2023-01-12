package io.craigmiller160.expensetrackerapi.service.parsing

import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.function.TryEither
import java.io.InputStream
import java.util.UUID

interface TransactionParser {
  fun parse(userId: UUID, stream: InputStream): TryEither<List<Transaction>>
}
