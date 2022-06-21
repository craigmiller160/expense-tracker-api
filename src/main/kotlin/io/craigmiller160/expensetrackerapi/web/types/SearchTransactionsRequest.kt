package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.springframework.format.annotation.DateTimeFormat

data class SearchTransactionsRequest(
    override val pageNumber: Int,
    override val pageSize: Int,
    @field:DateTimeFormat(pattern = "yyyy-MM-dd") val startDate: LocalDate? = null,
    @field:DateTimeFormat(pattern = "yyyy-MM-dd") val endDate: LocalDate? = null,
    val confirmed: Boolean? = null,
    val categoryIds: Set<TypedId<CategoryId>>? = null
) : PageableRequest {
  companion object {
    private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  }
  fun toQueryString(): String =
      sequenceOf(
              "pageNumber" to pageNumber,
              "pageSize" to pageSize,
              "startDate" to startDate?.let { DATE_FORMAT.format(it) },
              "endDate" to endDate?.let { DATE_FORMAT.format(it) },
              "confirmed" to confirmed,
              "categoryIds" to categoryIds?.map { it.toString() })
          .filter { it.second != null }
          .map { "${it.first}=${it.second}" }
          .joinToString("&")
}
