package io.craigmiller160.expensetrackerapi.web.types.transaction

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.utils.DateUtils
import io.craigmiller160.expensetrackerapi.data.model.YesNoFilter
import io.craigmiller160.expensetrackerapi.web.types.PageableRequest
import io.craigmiller160.expensetrackerapi.web.types.SortDirection
import io.craigmiller160.expensetrackerapi.web.types.SortableRequest
import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat

data class SearchTransactionsRequest(
    @field:Min(0) override val pageNumber: Int,
    @field:Max(100) override val pageSize: Int,
    override val sortKey: TransactionSortKey,
    override val sortDirection: SortDirection,
    @field:DateTimeFormat(pattern = DateUtils.DATE_PATTERN) val startDate: LocalDate? = null,
    @field:DateTimeFormat(pattern = DateUtils.DATE_PATTERN) val endDate: LocalDate? = null,
    val confirmed: YesNoFilter = YesNoFilter.ALL,
    val categorized: YesNoFilter = YesNoFilter.ALL,
    val duplicate: YesNoFilter = YesNoFilter.ALL,
    val possibleRefund: YesNoFilter = YesNoFilter.ALL,
    val categoryIds: Set<TypedId<CategoryId>>? = null,
) : PageableRequest, SortableRequest<TransactionSortKey> {

  @Hidden
  @AssertTrue(message = "Cannot set categorized to NO and specify categoryIds")
  fun isCategoryPropsValid(): Boolean {
    if (YesNoFilter.NO == categorized) {
      return categoryIds.isNullOrEmpty()
    }
    return true
  }

  fun toQueryString(): String =
      sequenceOf<Pair<String, String?>>(
              "pageNumber" to pageNumber.toString(),
              "pageSize" to pageSize.toString(),
              "sortKey" to sortKey.name,
              "sortDirection" to sortDirection.name,
              "startDate" to startDate?.let { DateUtils.format(it) },
              "endDate" to endDate?.let { DateUtils.format(it) },
              "confirmed" to confirmed.name,
              "categorized" to categorized.name,
              "duplicate" to duplicate?.name,
              "possibleRefund" to possibleRefund?.name,
              "categoryIds" to categoryIds?.joinToString(",") { it.toString() })
          .filter { it.second != null }
          .map { (first, second) -> first to URLEncoder.encode(second, StandardCharsets.UTF_8) }
          .map { "${it.first}=${it.second}" }
          .joinToString("&")
}
