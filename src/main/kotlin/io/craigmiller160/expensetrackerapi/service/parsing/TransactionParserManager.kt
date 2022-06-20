package io.craigmiller160.expensetrackerapi.service.parsing

import io.craigmiller160.expensetrackerapi.service.TransactionImportType
import org.springframework.stereotype.Component

@Component
class TransactionParserManager(
    private val discoverCsvParser: DiscoverCsvTransactionParser,
    private val chaseCsvParser: ChaseCsvTransactionParser
) {
  fun getParserForType(type: TransactionImportType): TransactionParser =
      when (type) {
        TransactionImportType.DISCOVER_CSV -> discoverCsvParser
        TransactionImportType.CHASE_CSV -> chaseCsvParser
      }
}
