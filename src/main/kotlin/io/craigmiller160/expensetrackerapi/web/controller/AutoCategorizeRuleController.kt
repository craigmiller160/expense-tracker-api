package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.service.AutoCategorizeRuleService
import io.craigmiller160.expensetrackerapi.web.types.rules.*
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
    responseCode = "200",
    content =
      [
        Content(
          mediaType = "application/json",
          array =
            ArraySchema(schema = Schema(implementation = AutoCategorizeRulePageResponse::class)))])
  @GetMapping
  fun getAllRules(
    request: AutoCategorizeRulePageRequest
  ): TryEither<AutoCategorizeRulePageResponse> = autoCategorizeRuleService.getAllRules(request)

  @ApiResponse(
    responseCode = "200",
    content =
      [
        Content(
          mediaType = "application/json",
          array =
            ArraySchema(schema = Schema(implementation = AutoCategorizeRuleResponse::class)))])
  @PostMapping
  fun createRule(
    @RequestBody request: AutoCategorizeRuleRequest
  ): TryEither<AutoCategorizeRuleResponse> = autoCategorizeRuleService.createRule(request)

  @ApiResponse(
    responseCode = "200",
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
  ): TryEither<AutoCategorizeRuleResponse> = autoCategorizeRuleService.updateRule(ruleId, request)

  @ApiResponse(
    responseCode = "200",
    content =
      [
        Content(
          mediaType = "application/json",
          array =
            ArraySchema(schema = Schema(implementation = AutoCategorizeRuleResponse::class)))])
  @GetMapping("/{ruleId}")
  fun getRule(
    @PathVariable ruleId: TypedId<AutoCategorizeRuleId>
  ): TryEither<AutoCategorizeRuleResponse> = autoCategorizeRuleService.getRule(ruleId)

  @ApiResponse(
    responseCode = "204",
    content =
      [
        Content(
          mediaType = "application/json", array = ArraySchema(schema = Schema(hidden = true)))])
  @DeleteMapping("/{ruleId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteRule(@PathVariable ruleId: TypedId<AutoCategorizeRuleId>): TryEither<Unit> =
    autoCategorizeRuleService.deleteRule(ruleId)

  @ApiResponse(
    responseCode = "204",
    content =
      [
        Content(
          mediaType = "application/json", array = ArraySchema(schema = Schema(hidden = true)))])
  @PutMapping("/{ruleId}/reOrder/{ordinal}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun reOrderRule(
    @PathVariable ruleId: TypedId<AutoCategorizeRuleId>,
    @PathVariable ordinal: Int
  ): TryEither<Unit> = autoCategorizeRuleService.reOrderRule(ruleId, ordinal)

  @ApiResponse(
    responseCode = "200",
    content =
      [
        Content(
          mediaType = "application/json",
          array = ArraySchema(schema = Schema(implementation = MaxOrdinalResponse::class)))])
  @GetMapping("/maxOrdinal")
  fun getMaxOrdinal(): TryEither<MaxOrdinalResponse> = autoCategorizeRuleService.getMaxOrdinal()
}
