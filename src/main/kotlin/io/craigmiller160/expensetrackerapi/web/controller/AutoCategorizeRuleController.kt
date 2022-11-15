package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageRequest
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageResponse
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRuleRequest
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRuleResponse
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories/rules")
class AutoCategorizeRuleController {
  @ApiResponse(
    content =
      [
        Content(
          mediaType = "application/json",
          array =
            ArraySchema(schema = Schema(implementation = AutoCategorizeRulePageResponse::class)))])
  @GetMapping
  fun getAllRules(
    request: AutoCategorizeRulePageRequest
  ): TryEither<AutoCategorizeRulePageResponse> = TODO()

  @ApiResponse(
    content =
      [
        Content(
          mediaType = "application/json",
          array =
            ArraySchema(schema = Schema(implementation = AutoCategorizeRuleResponse::class)))])
  @PostMapping
  fun createRule(
    @RequestBody request: AutoCategorizeRuleRequest
  ): TryEither<AutoCategorizeRuleResponse> = TODO()

  @ApiResponse(
    content =
      [
        Content(
          mediaType = "application/json",
          array =
            ArraySchema(schema = Schema(implementation = AutoCategorizeRuleResponse::class)))])
  @PutMapping("/{ruleId}")
  fun updateRule(
    @PathVariable ruleId: TypedId<AutoCategorizeRuleId>,
    @RequestBody request: AutoCategorizeRuleRequest
  ): TryEither<AutoCategorizeRuleResponse> = TODO()
}
