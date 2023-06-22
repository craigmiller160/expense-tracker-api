package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.service.TransactionService
import io.craigmiller160.expensetrackerapi.web.types.*
import io.craigmiller160.expensetrackerapi.web.types.transaction.*
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transactions")
class TransactionController(private val transactionService: TransactionService) {
  @ApiResponse(
      responseCode = "200",
      content =
          [
              Content(
                  mediaType = "application/json",
                  schema = Schema(implementation = TransactionsPageResponse::class))])
  @GetMapping
  fun search(@Valid request: SearchTransactionsRequest): TryEither<TransactionsPageResponse> =
      transactionService.search(request)

  @ApiResponse(
      responseCode = "200",
      content =
          [
              Content(
                  mediaType = "application/json",
                  schema = Schema(implementation = TransactionResponse::class))])
  @PostMapping
  fun createTransaction(
      @RequestBody request: CreateTransactionRequest
  ): TryEither<TransactionResponse> = transactionService.createTransaction(request)

  @ApiResponse(
      responseCode = "204",
      content = [Content(mediaType = "application/json", schema = Schema(hidden = true))])
  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteTransactions(@RequestBody request: DeleteTransactionsRequest): TryEither<Unit> =
      transactionService.deleteTransactions(request)

  @ApiResponse(
      responseCode = "204",
      content = [Content(mediaType = "application/json", schema = Schema(hidden = true))])
  @PutMapping("/categorize")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun categorizeTransactions(@RequestBody request: CategorizeTransactionsRequest): TryEither<Unit> =
      transactionService.categorizeTransactions(request.transactionsAndCategories)

  @ApiResponse(
      responseCode = "204",
      content = [Content(mediaType = "application/json", schema = Schema(hidden = true))])
  @PutMapping("/confirm")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun confirmTransactions(@RequestBody request: ConfirmTransactionsRequest): TryEither<Unit> =
      transactionService.confirmTransactions(request.transactionsToConfirm)

  @ApiResponse(
      responseCode = "204",
      content = [Content(mediaType = "application/json", schema = Schema(hidden = true))])
  @PutMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updateTransactions(@RequestBody request: UpdateTransactionsRequest): TryEither<Unit> =
      transactionService.updateTransactions(request)

  @ApiResponse(
      responseCode = "204",
      content = [Content(mediaType = "application/json", schema = Schema(hidden = true))])
  @PutMapping("/{transactionId}/details")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updateTransactionDetails(
      @PathVariable("transactionId") transactionId: TypedId<TransactionId>,
      @RequestBody request: UpdateTransactionDetailsRequest
  ): TryEither<Unit> = transactionService.updateTransactionDetails(transactionId, request)

  @ApiResponse(
      responseCode = "200",
      content =
          [
              Content(
                  mediaType = "application/json",
                  schema = Schema(implementation = TransactionDuplicatePageResponse::class))])
  @GetMapping("/{transactionId}/duplicates")
  fun getPossibleDuplicates(
      @PathVariable transactionId: TypedId<TransactionId>,
      request: GetPossibleDuplicatesRequest
  ): TryEither<TransactionDuplicatePageResponse> =
      transactionService.getPossibleDuplicates(transactionId, request)

  @ApiResponse(
      responseCode = "204",
      content = [Content(mediaType = "application/json", schema = Schema(hidden = true))])
  @PutMapping("/{transactionId}/notDuplicate")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun markNotDuplicate(@PathVariable transactionId: TypedId<TransactionId>): TryEither<Unit> =
      transactionService.markNotDuplicate(transactionId)

  @ApiResponse(
      responseCode = "200",
      content =
          [
              Content(
                  mediaType = "application/json",
                  schema = Schema(implementation = TransactionDetailsResponse::class))])
  @GetMapping("/{transactionId}/details")
  fun getTransactionDetails(
      @PathVariable transactionId: TypedId<TransactionId>
  ): TryEither<TransactionDetailsResponse> = transactionService.getTransactionDetails(transactionId)

  @ApiResponse(
      responseCode = "200",
      content =
          [
              Content(
                  mediaType = "application/json",
                  schema = Schema(implementation = DeleteTransactionsResponse::class))])
  @DeleteMapping("/unconfirmed")
  fun deleteAllUnconfirmed(): TryEither<DeleteTransactionsResponse> =
      transactionService.deleteAllUnconfirmed()
}
