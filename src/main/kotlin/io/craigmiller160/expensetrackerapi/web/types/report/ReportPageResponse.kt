package io.craigmiller160.expensetrackerapi.web.types.report

import io.craigmiller160.expensetrackerapi.web.types.PageableResponse

data class ReportPageResponse(
  val reports: List<ReportMonthResponse>,
  override val pageNumber: Int,
  override val totalItems: Long
) : PageableResponse
