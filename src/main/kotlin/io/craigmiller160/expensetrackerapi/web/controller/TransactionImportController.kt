package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.service.TransactionImportService
import io.craigmiller160.expensetrackerapi.service.TransactionImportType
import io.craigmiller160.expensetrackerapi.web.types.ImportTypeResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/transaction-import")
class TransactionImportController(private val transactionImportService: TransactionImportService) {
  @PostMapping
  fun importTransactions(
      @RequestParam("type") type: TransactionImportType,
      @RequestParam("file") file: MultipartFile
  ) {
    TODO()
  }

  @GetMapping("/types")
  fun getImportTypes(): List<ImportTypeResponse> = transactionImportService.getImportTypes()
}
