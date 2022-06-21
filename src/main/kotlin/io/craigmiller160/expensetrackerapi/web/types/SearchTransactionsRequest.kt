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
) : PageableRequest {
  fun toQueryString(): String =
      sequenceOf(
              "pageNumber" to pageNumber,
              "pageSize" to pageSize,
              "startDate" to startDate,
              "endDate" to endDate,
              "confirmed" to confirmed,
              "categoryIds" to categoryIds?.map { it.toString() })
          .filter { it.second != null }
          .map { "${it.first}=${it.second}" }
          .joinToString("&")
}
