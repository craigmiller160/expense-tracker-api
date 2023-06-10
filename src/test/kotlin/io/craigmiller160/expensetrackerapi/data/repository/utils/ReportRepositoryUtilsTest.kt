package io.craigmiller160.expensetrackerapi.data.repository.utils

import io.craigmiller160.expensetrackerapi.web.types.report.ReportCategoryIdFilterType
import java.util.stream.Stream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ReportRepositoryUtilsTest {
  companion object {
    @JvmStatic fun queryFilterTypeConfigs(): Stream<QueryFilterTypeConfig> = TODO()
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
    val expectedResult: ReportQueryCategoryFilterType
)
