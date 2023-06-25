package io.craigmiller160.expensetrackerapi.web.types.transaction

import io.craigmiller160.expensetrackerapi.web.types.PageableRequest

// TODO needs validation
data class GetPossibleDuplicatesRequest(override val pageNumber: Int, override val pageSize: Int) :
    PageableRequest
