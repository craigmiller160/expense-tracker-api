package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.service.TransactionService
import io.craigmiller160.expensetrackerapi.web.types.CategorizeTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.DeleteTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.NeedsAttentionResponse
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsResponse
import javax.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transactions")
class TransactionController(private val transactionService: TransactionService) {
  @GetMapping
  fun search(@Valid request: SearchTransactionsRequest): TryEither<SearchTransactionsResponse> =
      transactionService.search(request)

  @DeleteMapping
  fun deleteTransactions(
      @RequestBody request: DeleteTransactionsRequest
  ): TryEither<ResponseEntity<Unit>> =
      transactionService.deleteTransactions(request).map { ResponseEntity.noContent().build() }

  @PutMapping("/categorize")
  fun categorizeTransactions(
      @RequestBody request: CategorizeTransactionsRequest
  ): TryEither<ResponseEntity<Unit>> =
      transactionService.categorizeTransactions(request).map { ResponseEntity.noContent().build() }

  @GetMapping("/needs-attention")
  fun getNeedsAttention(): TryEither<NeedsAttentionResponse> =
      transactionService.getNeedsAttention()
}
