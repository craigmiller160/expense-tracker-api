package io.craigmiller160.expensetrackerapi.web.types

import java.time.LocalDate

data class TransactionSearchRequest(
    override val pageNumber: Int,
    override val pageSize: Int,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val confirmed: Boolean? = null
) : PageableRequest
