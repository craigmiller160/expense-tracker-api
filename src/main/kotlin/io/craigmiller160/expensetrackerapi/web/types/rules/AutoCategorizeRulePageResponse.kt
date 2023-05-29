package io.craigmiller160.expensetrackerapi.web.types.rules

import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRuleView
import io.craigmiller160.expensetrackerapi.web.types.PageableResponse
import org.springframework.data.domain.Page

data class AutoCategorizeRulePageResponse(
    val rules: List<AutoCategorizeRuleResponse>,
    override val pageNumber: Int,
    override val totalItems: Long
) : PageableResponse {
  companion object {
    fun from(page: Page<AutoCategorizeRuleView>): AutoCategorizeRulePageResponse =
        AutoCategorizeRulePageResponse(
            rules = page.content.map { AutoCategorizeRuleResponse.from(it) },
            pageNumber = page.number,
            totalItems = page.totalElements)
  }
}
