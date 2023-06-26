package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.service.NeedsAttentionService
import io.craigmiller160.expensetrackerapi.web.types.transaction.NeedsAttentionResponse
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/needs-attention")
class NeedsAttentionController(private val needsAttentionService: NeedsAttentionService) {
  @ApiResponse(
      responseCode = "200",
      content =
          [
              Content(
                  mediaType = "application/json",
                  schema = Schema(implementation = NeedsAttentionResponse::class))])
  @GetMapping
  fun getNeedsAttention(): TryEither<NeedsAttentionResponse> =
      needsAttentionService.getNeedsAttention()
}
