package io.craigmiller160.expensetrackerapi.web.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/transaction-import")
class TransactionImportController {
  @PostMapping
  fun importTransactions(file: MultipartFile) {
    TODO()
  }
}
