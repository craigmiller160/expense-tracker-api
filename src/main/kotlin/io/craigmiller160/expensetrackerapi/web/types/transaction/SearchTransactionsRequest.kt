package io.craigmiller160.expensetrackerapi.web.types.transaction

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.utils.DateUtils
import io.craigmiller160.expensetrackerapi.web.types.PageableRequest
import io.craigmiller160.expensetrackerapi.web.types.SortDirection
import io.craigmiller160.expensetrackerapi.web.types.SortableRequest
import java.time.LocalDate
import javax.validation.constraints.AssertTrue
import org.springframework.format.annotation.DateTimeFormat

data class SearchTransactionsRequest(
  override val pageNumber: Int,
  override val pageSize: Int,
  override val sortKey: TransactionSortKey,
  override val sortDirection: SortDirection,
  @field:DateTimeFormat(pattern = DateUtils.DATE_PATTERN) val startDate: LocalDate? = null,
  @field:DateTimeFormat(pattern = DateUtils.DATE_PATTERN) val endDate: LocalDate? = null,
  val isConfirmed: Boolean? = null,
  val isCategorized: Boolean? = null,
  val isDuplicate: Boolean? = null,
  val isPossibleRefund: Boolean? = null,
  val categoryIds: Set<TypedId<CategoryId>>? = null,
) : PageableRequest, SortableRequest<TransactionSortKey> {

  @AssertTrue(message = "Cannot set WITHOUT_CATEGORY and specify categoryIds")
  fun isCategoryPropsValid(): Boolean {
    if (isCategorized == false) {
      return categoryIds.isNullOrEmpty()
    }
    return true
  }

  fun toQueryString(): String =
    sequenceOf(
        "pageNumber" to pageNumber,
        "pageSize" to pageSize,
        "sortKey" to sortKey.name,
        "sortDirection" to sortDirection.name,
        "startDate" to startDate?.let { DateUtils.format(it) },
        "endDate" to endDate?.let { DateUtils.format(it) },
        "isConfirmed" to isConfirmed,
        "isCategorized" to isCategorized,
        "isDuplicate" to isDuplicate,
        "isPossibleRefund" to isPossibleRefund,
        "categoryIds" to categoryIds?.joinToString(",") { it.toString() })
      .filter { it.second != null }
      .map { "${it.first}=${it.second}" }
      .joinToString("&")
}
