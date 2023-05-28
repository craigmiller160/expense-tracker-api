package io.craigmiller160.expensetrackerapi.web.types.rules

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.web.types.PageableRequest

data class AutoCategorizeRulePageRequest(
    override val pageNumber: Int,
    override val pageSize: Int,
    val categoryId: TypedId<CategoryId>? = null,
    val regex: String? = null
) : PageableRequest
