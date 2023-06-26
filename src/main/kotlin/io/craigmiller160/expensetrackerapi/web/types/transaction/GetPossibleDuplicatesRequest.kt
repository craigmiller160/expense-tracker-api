package io.craigmiller160.expensetrackerapi.web.types.transaction

import io.craigmiller160.expensetrackerapi.web.types.PageableRequest
import io.craigmiller160.expensetrackerapi.web.types.QueryObject
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class GetPossibleDuplicatesRequest(
    @field:Min(0) override val pageNumber: Int,
    @field:Min(0) @field:Max(100) override val pageSize: Int
) : PageableRequest, QueryObject
