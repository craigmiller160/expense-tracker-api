package io.craigmiller160.expensetrackerapi.service

import arrow.core.flatMap
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.function.flatMapCatch
import io.craigmiller160.expensetrackerapi.service.parsing.TransactionParserManager
import io.craigmiller160.expensetrackerapi.web.types.importing.ImportTransactionsResponse
import io.craigmiller160.expensetrackerapi.web.types.importing.ImportTypeResponse
import io.craigmiller160.oauth2.service.OAuth2Service
import java.io.InputStream
import javax.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class TransactionImportService(
  private val transactionRepository: TransactionRepository,
  private val transactionParserManager: TransactionParserManager,
  private val oAuth2Service: OAuth2Service,
  private val applyCategoriesToTransactionsService: ApplyCategoriesToTransactionsService
) {
  fun getImportTypes(): List<ImportTypeResponse> =
    TransactionImportType.values().map { ImportTypeResponse(it.name, it.displayName) }

  @Transactional
  fun importTransactions(
    type: TransactionImportType,
    stream: InputStream
  ): TryEither<ImportTransactionsResponse> {
    val parser = transactionParserManager.getParserForType(type)
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return parser
      .parse(userId, stream)
      .flatMapCatch { transactions -> transactionRepository.saveAll(transactions) }
      .flatMap { transactions ->
        applyCategoriesToTransactionsService.applyCategoriesToTransactions(userId, transactions)
      }
      .map { transactions -> ImportTransactionsResponse(transactions.size) }
  }
}
