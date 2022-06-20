package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import arrow.core.continuations.either
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.service.parsing.TransactionParserManager
import io.craigmiller160.expensetrackerapi.web.types.ImportTransactionsResponse
import io.craigmiller160.expensetrackerapi.web.types.ImportTypeResponse
import java.io.InputStream
import org.springframework.stereotype.Service

@Service
class TransactionImportService(
    private val transactionRepository: TransactionRepository,
    private val transactionParserManager: TransactionParserManager
) {
  fun getImportTypes(): List<ImportTypeResponse> =
      TransactionImportType.values().map { ImportTypeResponse(it.name, it.displayName) }

  fun importTransactions(
      type: TransactionImportType,
      stream: InputStream
  ): TryEither<ImportTransactionsResponse> =
      either.eager {
        val rawTxns = Either.catch { stream.use { it.reader().readText() } }.bind()
        val parser = transactionParserManager.getParserForType(type)
        val transactions = parser.parse(rawTxns).bind()
        Either.catch { transactionRepository.saveAll(transactions) }.bind()
        ImportTransactionsResponse(transactions.size)
      }
}
