package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.service.TransactionService
import io.craigmiller160.expensetrackerapi.web.types.CategorizeTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.ConfirmTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.DeleteTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.NeedsAttentionResponse
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsResponse
import io.craigmiller160.expensetrackerapi.web.types.UpdateTransactionDetailsRequest
import io.craigmiller160.expensetrackerapi.web.types.UpdateTransactionsRequest
import javax.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transactions")
class TransactionController(private val transactionService: TransactionService) {
  @GetMapping
  fun search(@Valid request: SearchTransactionsRequest): TryEither<SearchTransactionsResponse> =
    transactionService.search(request)

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteTransactions(@RequestBody request: DeleteTransactionsRequest): TryEither<Unit> =
    transactionService.deleteTransactions(request)

  @PutMapping("/categorize")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun categorizeTransactions(@RequestBody request: CategorizeTransactionsRequest): TryEither<Unit> =
    transactionService.categorizeTransactions(request.transactionsAndCategories)

  @PutMapping("/confirm")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun confirmTransactions(@RequestBody request: ConfirmTransactionsRequest): TryEither<Unit> =
    transactionService.confirmTransactions(request.transactionsToConfirm)

  @GetMapping("/needs-attention")
  fun getNeedsAttention(): TryEither<NeedsAttentionResponse> =
    transactionService.getNeedsAttention()

  @PutMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updateTransactions(@RequestBody request: UpdateTransactionsRequest): TryEither<Unit> =
    transactionService.updateTransactions(request)

  @PutMapping("/{transactionId}/details")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updateTransactionDetails(
    transactionId: TypedId<TransactionId>,
    @RequestBody request: UpdateTransactionDetailsRequest
  ): TryEither<Unit> = transactionService.updateTransactionDetails(transactionId, request)
}
