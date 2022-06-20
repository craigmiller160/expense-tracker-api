package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import java.time.LocalDate

data class SearchTransactionsRequest(
    override val pageNumber: Int,
    override val pageSize: Int,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val confirmed: Boolean? = null,
    val categoryIds: Set<TypedId<CategoryId>>? = null
) : PageableRequest
