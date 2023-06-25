package io.craigmiller160.expensetrackerapi.web.types.report

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.web.types.PageableRequest
import io.craigmiller160.expensetrackerapi.web.types.QueryObject
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class ReportRequest(
    @Min(0) override val pageNumber: Int,
    @Max(100) override val pageSize: Int,
    val categoryIdType: ReportCategoryIdFilterType = ReportCategoryIdFilterType.EXCLUDE,
    val categoryIds: List<TypedId<CategoryId>> = listOf()
) : PageableRequest, QueryObject {
  override fun fieldsToQueryParams(): List<Pair<String, String?>> =
      listOf(
          "pageNumber" to pageNumber.toString(),
          "pageSize" to pageSize.toString(),
          "categoryIdType" to categoryIdType.name,
          "categoryIds" to categoryIds.joinToString(",") { it.toString() })
}
