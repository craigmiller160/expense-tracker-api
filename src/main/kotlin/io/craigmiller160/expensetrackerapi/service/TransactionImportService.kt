package io.craigmiller160.expensetrackerapi.service

import io.craigmiller160.expensetrackerapi.web.types.ImportTypeResponse
import java.io.InputStream
import org.springframework.stereotype.Service

@Service
class TransactionImportService {
  fun getImportTypes(): List<ImportTypeResponse> =
      TransactionImportType.values().map { ImportTypeResponse(it.name, it.displayName) }

  fun importTransactions(type: TransactionImportType, stream: InputStream) {
    TODO()
  }
}
