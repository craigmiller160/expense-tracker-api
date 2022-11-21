package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.web.types.rules.LastRuleAppliedResponse
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transactions/rules/last-applied")
class LastRuleAppliedController {

  @ApiResponse(
    content =
      [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = LastRuleAppliedResponse::class))])
  @GetMapping("/{transactionId}")
  fun getLastAppliedRuleForTransaction(
    @PathVariable transactionId: TypedId<TransactionId>
  ): TryEither<LastRuleAppliedResponse> {
    TODO()
  }
}
