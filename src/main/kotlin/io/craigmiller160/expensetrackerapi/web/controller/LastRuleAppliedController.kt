package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.service.LastRuleAppliedService
import io.craigmiller160.expensetrackerapi.web.types.rules.LastRuleAppliedResponse
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transactions/rules/last-applied")
class LastRuleAppliedController(private val lastRuleAppliedService: LastRuleAppliedService) {

  @ApiResponses(
    value =
      [
        ApiResponse(
          responseCode = "200",
          content =
            [
              Content(
                mediaType = "application/json",
                schema = Schema(implementation = LastRuleAppliedResponse::class))]),
        ApiResponse(responseCode = "204", content = [Content(schema = Schema(hidden = true))])])
  @GetMapping("/{transactionId}")
  fun getLastAppliedRuleForTransaction(
    @PathVariable transactionId: TypedId<TransactionId>
  ): TryEither<ResponseEntity<LastRuleAppliedResponse>> =
    lastRuleAppliedService.getLastAppliedRuleForTransaction(transactionId).map { result ->
      result?.let { ResponseEntity.ok(it) } ?: ResponseEntity.noContent().build()
    }
}
