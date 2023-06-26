package io.craigmiller160.expensetrackerapi.web.types.transaction

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.utils.DateUtils
import io.craigmiller160.expensetrackerapi.data.model.YesNoFilter
import io.craigmiller160.expensetrackerapi.web.types.PageableRequest
import io.craigmiller160.expensetrackerapi.web.types.QueryObject
import io.craigmiller160.expensetrackerapi.web.types.SortDirection
import io.craigmiller160.expensetrackerapi.web.types.SortableRequest
import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat

data class SearchTransactionsRequest(
    @field:Min(0) override val pageNumber: Int,
    @field:Min(0) @field:Max(100) override val pageSize: Int,
    override val sortKey: TransactionSortKey,
    override val sortDirection: SortDirection,
    @field:DateTimeFormat(pattern = DateUtils.DATE_PATTERN) val startDate: LocalDate? = null,
    @field:DateTimeFormat(pattern = DateUtils.DATE_PATTERN) val endDate: LocalDate? = null,
    val confirmed: YesNoFilter = YesNoFilter.ALL,
    val categorized: YesNoFilter = YesNoFilter.ALL,
    val duplicate: YesNoFilter = YesNoFilter.ALL,
    val possibleRefund: YesNoFilter = YesNoFilter.ALL,
    val categoryIds: Set<TypedId<CategoryId>>? = null,
    val descriptionRegex: String? = null
) : PageableRequest, SortableRequest<TransactionSortKey>, QueryObject {

  @Hidden
  @AssertTrue(message = "Cannot set categorized to NO and specify categoryIds")
  fun isCategorizedValidation(): Boolean {
    if (YesNoFilter.NO == categorized) {
      return categoryIds.isNullOrEmpty()
    }
    return true
  }
}
