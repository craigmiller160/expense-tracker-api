package io.craigmiller160.expensetrackerapi.web.types.report

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.web.types.PageableRequest

data class ReportRequest(
  override val pageNumber: Int,
  override val pageSize: Int,
  val excludeCategoryIds: List<TypedId<CategoryId>> = listOf()
) : PageableRequest
