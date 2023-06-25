package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.service.ReportService
import io.craigmiller160.expensetrackerapi.web.types.report.ReportPageResponse
import io.craigmiller160.expensetrackerapi.web.types.report.ReportRequest
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/reports")
class ReportController(private val reportService: ReportService) {
  @ApiResponse(
      responseCode = "200",
      content =
          [
              Content(
                  mediaType = "application/json",
                  schema = Schema(implementation = ReportPageResponse::class))])
  @GetMapping
  fun getSpendingByMonthAndCategory(@Valid request: ReportRequest): TryEither<ReportPageResponse> =
      reportService.getSpendingByMonthAndCategory(request)
}
