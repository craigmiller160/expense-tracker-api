package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.service.AutoCategorizeRuleService
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageRequest
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageResponse
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRuleRequest
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRuleResponse
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories/rules")
class AutoCategorizeRuleController(
  private val autoCategorizeRuleService: AutoCategorizeRuleService
) {
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
  ): TryEither<AutoCategorizeRulePageResponse> = autoCategorizeRuleService.getAllRules()

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

  @ApiResponse(
    content =
      [
        Content(
          mediaType = "application/json",
          array =
            ArraySchema(schema = Schema(implementation = AutoCategorizeRuleResponse::class)))])
  @GetMapping("/{ruleId}")
  fun getRule(
    @PathVariable ruleId: TypedId<AutoCategorizeRuleId>
  ): TryEither<AutoCategorizeRuleResponse> = TODO()

  @ApiResponse(
    content =
      [
        Content(
          mediaType = "application/json",
          array = ArraySchema(schema = Schema(implementation = Unit::class)))])
  @DeleteMapping("/{ruleId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  // TODO must fix all ordinals
  fun deleteRule(@PathVariable ruleId: TypedId<AutoCategorizeRuleId>): TryEither<Unit> = TODO()

  @ApiResponse(
    content =
      [
        Content(
          mediaType = "application/json",
          array = ArraySchema(schema = Schema(implementation = Unit::class)))])
  @PutMapping("/{ruleId}/reOrder/{ordinal}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  // TODO must change other record ordering too
  fun reOrderRule(
    @PathVariable ruleId: TypedId<AutoCategorizeRuleId>,
    @PathVariable ordinal: Int
  ): TryEither<Unit> = TODO()
}
