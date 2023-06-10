package io.craigmiller160.expensetrackerapi.data.repository.utils

import io.craigmiller160.expensetrackerapi.web.types.report.ReportCategoryIdFilterType
import java.lang.IllegalArgumentException

enum class ReportQueryCategoryFilterType {
  INCLUDE_NO_UNKNOWN,
  INCLUDE_WITH_UNKNOWN,
  EXCLUDE_NO_UNKNOWN,
  EXCLUDE_WITH_UNKNOWN,
  ALL_NO_UNKNOWN, // TODO might not be necessary
  ALL_WITH_UNKNOWN
}

// TODO probably want tests for this
fun ReportCategoryIdFilterType.toQueryType(
    hasUnknownId: Boolean,
    hasOtherIds: Boolean
): ReportQueryCategoryFilterType {
  if (ReportCategoryIdFilterType.INCLUDE == this) {
    if (hasUnknownId && hasOtherIds) {
      return ReportQueryCategoryFilterType.INCLUDE_WITH_UNKNOWN
    }

    if (!hasUnknownId && hasOtherIds) {
      return ReportQueryCategoryFilterType.INCLUDE_NO_UNKNOWN
    }

    return ReportQueryCategoryFilterType.ALL_WITH_UNKNOWN
  }

  if (ReportCategoryIdFilterType.EXCLUDE == this) {
    if (hasUnknownId && hasOtherIds) {
      return ReportQueryCategoryFilterType.EXCLUDE_NO_UNKNOWN
    }

    if (!hasUnknownId && hasOtherIds) {
      return ReportQueryCategoryFilterType.EXCLUDE_WITH_UNKNOWN
    }

    return ReportQueryCategoryFilterType.ALL_WITH_UNKNOWN
  }

  throw IllegalArgumentException("Invalid combination of query filter values")
}
