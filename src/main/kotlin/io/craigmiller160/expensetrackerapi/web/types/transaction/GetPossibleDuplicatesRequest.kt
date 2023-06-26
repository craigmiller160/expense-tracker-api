package io.craigmiller160.expensetrackerapi.web.types.transaction

import io.craigmiller160.expensetrackerapi.web.types.PageableRequest
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

// TODO needs validation
data class GetPossibleDuplicatesRequest(
    @field:Min(0) override val pageNumber: Int,
    @field:Max(100) override val pageSize: Int
) : PageableRequest
