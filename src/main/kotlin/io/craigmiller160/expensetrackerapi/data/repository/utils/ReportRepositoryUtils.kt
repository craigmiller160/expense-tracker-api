package io.craigmiller160.expensetrackerapi.data.repository.utils

import io.craigmiller160.expensetrackerapi.web.types.report.ReportCategoryIdFilterType
import java.lang.IllegalArgumentException

enum class ReportCategoryFilterType {
  INCLUDE_NO_UNKNOWN,
  INCLUDE_WITH_UNKNOWN,
  EXCLUDE_NO_UNKNOWN,
  EXCLUDE_WITH_UNKNOWN,
  ALL_NO_UNKNOWN,
  ALL_WITH_UNKNOWN,
  NONE_WITH_UNKNOWN
}

fun ReportCategoryIdFilterType.toQueryType(
    hasUnknownId: Boolean,
    hasOtherIds: Boolean
): ReportCategoryFilterType {
  if (ReportCategoryIdFilterType.INCLUDE == this) {
    if (hasUnknownId && hasOtherIds) {
      return ReportCategoryFilterType.INCLUDE_WITH_UNKNOWN
    }

    if (!hasUnknownId && hasOtherIds) {
      return ReportCategoryFilterType.INCLUDE_NO_UNKNOWN
    }

    if (hasUnknownId && !hasOtherIds) {
      return ReportCategoryFilterType.NONE_WITH_UNKNOWN
    }

    return ReportCategoryFilterType.ALL_WITH_UNKNOWN
  }

  if (ReportCategoryIdFilterType.EXCLUDE == this) {
    if (hasUnknownId && hasOtherIds) {
      return ReportCategoryFilterType.EXCLUDE_WITH_UNKNOWN
    }

    if (!hasUnknownId && hasOtherIds) {
      return ReportCategoryFilterType.EXCLUDE_NO_UNKNOWN
    }

    if (!hasUnknownId && !hasOtherIds) {
      return ReportCategoryFilterType.ALL_WITH_UNKNOWN
    }

    return ReportCategoryFilterType.ALL_NO_UNKNOWN
  }

  throw IllegalArgumentException("Invalid combination of query filter values")
}
