package io.craigmiller160.expensetrackerapi.web.types.rules

import io.craigmiller160.expensetrackerapi.web.types.PageableResponse

data class AutoCategorizeRulePageResponse(
  val rules: List<AutoCategorizeRuleResponse>,
  override val pageNumber: Int,
  override val totalItems: Long
) : PageableResponse
