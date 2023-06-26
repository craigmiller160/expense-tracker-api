package io.craigmiller160.expensetrackerapi.web.types.rules

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.web.types.PageableRequest
import io.craigmiller160.expensetrackerapi.web.types.QueryObject
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class AutoCategorizeRulePageRequest(
    @field:Min(0) override val pageNumber: Int,
    // TODO update tests for min
    @field:Min(0) @field:Max(100) override val pageSize: Int,
    val categoryId: TypedId<CategoryId>? = null,
    // This is used to lookup literal regexes in the database and doesn't need regex validation
    val regex: String? = null
) : PageableRequest, QueryObject
