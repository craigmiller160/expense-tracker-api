package io.craigmiller160.expensetrackerapi.web.types

import java.time.LocalDate

data class TransactionSearchRequest(
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val confirmed: Boolean?,
    override val pageNumber: Int,
    override val pageSize: Int
) : PageableRequest
