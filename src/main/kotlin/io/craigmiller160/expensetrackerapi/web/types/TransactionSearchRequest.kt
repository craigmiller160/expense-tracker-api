package io.craigmiller160.expensetrackerapi.web.types

data class TransactionSearchRequest(
    val confirmed: Boolean,
    override val pageNumber: Int,
    override val pageSize: Int
) : PageableRequest
