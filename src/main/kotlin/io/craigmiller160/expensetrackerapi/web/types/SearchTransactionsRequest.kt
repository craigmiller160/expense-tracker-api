package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.error.BadRequestException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.springframework.format.annotation.DateTimeFormat

const val DATE_PATTERN = "yyyy-MM-dd"

data class SearchTransactionsRequest(
    override val pageNumber: Int,
    override val pageSize: Int,
    override val sortKey: TransactionSortKey,
    override val sortDirection: SortDirection,
    @field:DateTimeFormat(pattern = DATE_PATTERN) val startDate: LocalDate? = null,
    @field:DateTimeFormat(pattern = DATE_PATTERN) val endDate: LocalDate? = null,
    val confirmed: Boolean? = null,
    val withNoCategory: Boolean? = null,
    val categoryIds: Set<TypedId<CategoryId>>? = null
) : PageableRequest, SortableRequest<TransactionSortKey> {
  companion object {
    private val DATE_FORMAT = DateTimeFormatter.ofPattern(DATE_PATTERN)
  }

  init {
    if (withNoCategory == true && categoryIds?.isNotEmpty() == true) {
      throw BadRequestException("Cannot set withNoCategory and specify categoryIds")
    }
  }

  fun toQueryString(): String =
      sequenceOf(
              "pageNumber" to pageNumber,
              "pageSize" to pageSize,
              "sortKey" to sortKey.name,
              "sortDirection" to sortDirection.name,
              "startDate" to startDate?.let { DATE_FORMAT.format(it) },
              "endDate" to endDate?.let { DATE_FORMAT.format(it) },
              "confirmed" to confirmed,
              "withNoCategory" to withNoCategory,
              "categoryIds" to categoryIds?.joinToString(",") { it.toString() })
          .filter { it.second != null }
          .map { "${it.first}=${it.second}" }
          .joinToString("&")
}
