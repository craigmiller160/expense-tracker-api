package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.web.types.CategorizeTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.DeleteTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.TransactionSearchRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transactions")
class TransactionController {
  @GetMapping
  fun search(request: TransactionSearchRequest) {
    TODO()
  }

  @DeleteMapping
  fun deleteTransactions(@RequestBody request: DeleteTransactionsRequest) {
    TODO()
  }

  @PutMapping("/categorize")
  fun categorizeTransactions(@RequestBody request: CategorizeTransactionsRequest) {
    TODO()
  }
}
