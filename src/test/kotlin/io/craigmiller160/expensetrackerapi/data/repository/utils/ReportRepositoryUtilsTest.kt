package io.craigmiller160.expensetrackerapi.data.repository.utils

import io.craigmiller160.expensetrackerapi.web.types.report.ReportCategoryIdFilterType
import java.util.stream.Stream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ReportRepositoryUtilsTest {
  companion object {
    @JvmStatic
    fun queryFilterTypeConfigs(): Stream<QueryFilterTypeConfig> =
        Stream.of(
            QueryFilterTypeConfig(
                ReportCategoryIdFilterType.INCLUDE,
                true,
                true,
                ReportCategoryFilterType.INCLUDE_WITH_UNKNOWN),
            QueryFilterTypeConfig(
                ReportCategoryIdFilterType.INCLUDE,
                true,
                false,
                ReportCategoryFilterType.NONE_WITH_UNKNOWN),
            QueryFilterTypeConfig(
                ReportCategoryIdFilterType.INCLUDE,
                false,
                true,
                ReportCategoryFilterType.INCLUDE_NO_UNKNOWN),
            QueryFilterTypeConfig(
                ReportCategoryIdFilterType.INCLUDE,
                false,
                false,
                ReportCategoryFilterType.ALL_WITH_UNKNOWN),
            QueryFilterTypeConfig(
                ReportCategoryIdFilterType.EXCLUDE,
                true,
                true,
                ReportCategoryFilterType.EXCLUDE_NO_UNKNOWN),
            QueryFilterTypeConfig(
                ReportCategoryIdFilterType.EXCLUDE,
                true,
                false,
                ReportCategoryFilterType.ALL_NO_UNKNOWN),
            QueryFilterTypeConfig(
                ReportCategoryIdFilterType.EXCLUDE,
                false,
                true,
                ReportCategoryFilterType.EXCLUDE_WITH_UNKNOWN),
            QueryFilterTypeConfig(
                ReportCategoryIdFilterType.EXCLUDE,
                false,
                false,
                ReportCategoryFilterType.ALL_WITH_UNKNOWN))
  }
  @ParameterizedTest
  @MethodSource("queryFilterTypeConfigs")
  fun `identifies the correct query filter type`(config: QueryFilterTypeConfig) {
    val actualResult =
        config.categoryIdFilterType.toQueryType(config.hasUnknown, config.hasOtherIds)
    assertEquals(config.expectedResult, actualResult)
  }
}

data class QueryFilterTypeConfig(
    val categoryIdFilterType: ReportCategoryIdFilterType,
    val hasUnknown: Boolean,
    val hasOtherIds: Boolean,
    val expectedResult: ReportCategoryFilterType
)
