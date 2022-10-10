package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import arrow.core.flatMap
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.function.flatMapCatch
import io.craigmiller160.expensetrackerapi.service.parsing.TransactionParserManager
import io.craigmiller160.expensetrackerapi.web.types.ImportTransactionsResponse
import io.craigmiller160.expensetrackerapi.web.types.ImportTypeResponse
import io.craigmiller160.oauth2.service.OAuth2Service
import java.io.InputStream
import org.springframework.stereotype.Service

@Service
class TransactionImportService(
  private val transactionRepository: TransactionRepository,
  private val transactionParserManager: TransactionParserManager,
  private val oAuth2Service: OAuth2Service
) {
  fun getImportTypes(): List<ImportTypeResponse> =
    TransactionImportType.values().map { ImportTypeResponse(it.name, it.displayName) }

  fun importTransactions(
    type: TransactionImportType,
    stream: InputStream
  ): TryEither<ImportTransactionsResponse> {
    val parser = transactionParserManager.getParserForType(type)
    val authUser = oAuth2Service.getAuthenticatedUser()
    return parser
      .parse(authUser.userId, stream)
      .flatMapCatch { transactions -> transactionRepository.saveAll(transactions) }
      .flatMap { transactions -> checkForDuplicates(transactions) }
      .map { transactions -> ImportTransactionsResponse(transactions.size) }
  }

  private fun checkForDuplicates(transactions: List<Transaction>): TryEither<List<Transaction>> =
    Either.catch {
      val hashes = transactions.map { it.contentHash }
      val dbDuplicates

      TODO()
    }
}
