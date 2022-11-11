package io.craigmiller160.expensetrackerapi.web.types.report

import io.craigmiller160.expensetrackerapi.web.types.PageableRequest

data class ReportRequest(override val pageNumber: Int, override val pageSize: Int) :
  PageableRequest
