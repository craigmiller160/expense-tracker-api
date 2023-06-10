package io.craigmiller160.expensetrackerapi.data.repository.impl

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.data.SqlLoader
import io.craigmiller160.expensetrackerapi.data.constants.CategoryConstants
import io.craigmiller160.expensetrackerapi.data.projection.SpendingByCategory
import io.craigmiller160.expensetrackerapi.data.projection.SpendingByMonth
import io.craigmiller160.expensetrackerapi.data.repository.ReportRepository
import io.craigmiller160.expensetrackerapi.web.types.report.ReportCategoryIdFilterType
import io.craigmiller160.expensetrackerapi.web.types.report.ReportRequest
import jakarta.transaction.Transactional
import java.lang.IllegalArgumentException
import java.time.LocalDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class ReportRepositoryImpl(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val sqlLoader: SqlLoader
) : ReportRepository {
  @Transactional
  override fun getSpendingByMonthAndCategory(
      userId: TypedId<UserId>,
      request: ReportRequest
  ): Page<SpendingByMonth> {
    val spendingByMonth =
        getSpendingByMonth(userId, request, request.categoryIdType, request.categoryIds)
    val spendingByMonthCount =
        getSpendingByMonthCount(userId, request.categoryIdType, request.categoryIds)
    val months = spendingByMonth.map { it.month }

    val fullResults =
        if (months.isNotEmpty()) {
          val spendingByCategory =
              getSpendingByCategoryForMonths(
                  userId, months, request.categoryIdType, request.categoryIds)
          spendingByMonth.map { monthRecord ->
            monthRecord.copy(
                categories =
                    // The records should already be in the correct order
                    spendingByCategory.filter { categoryRecord ->
                      categoryRecord.month == monthRecord.month
                    })
          }
        } else {
          listOf()
        }

    return PageImpl(
        fullResults, PageRequest.of(request.pageNumber, request.pageSize), spendingByMonthCount)
  }

  private fun getSpendingByCategoryForMonths(
      userId: TypedId<UserId>,
      months: List<LocalDate>,
      categoryIdType: ReportCategoryIdFilterType,
      categoryIds: List<TypedId<CategoryId>>
  ): List<SpendingByCategory> {
    val getSpendingByCategoryForMonthSql =
        sqlLoader.loadSql("reports/get_spending_by_category_for_month.sql")
    val finalWrapper =
        months
            .mapIndexed { index, month ->
              val sql =
                  getSpendingByCategoryForMonthSql
                      .replace(":theDate", ":theDate$index")
                      .replace(";", "")
              val params = mapOf("theDate$index" to month)
              SpendingByCategoryQueryWrapper(params = params, sql = sql)
            }
            .reduce { acc, record ->
              SpendingByCategoryQueryWrapper(
                  params = acc.params + record.params, sql = "(${acc.sql})\nUNION\n(${record.sql})")
            }
            .let { wrapper ->
              if (months.size > 1) {
                // Order By at the end is necessary because the union breaks the ordering for each
                // individual query
                wrapper.copy(sql = "${wrapper.sql} ORDER BY category_name ASC")
              } else {
                wrapper
              }
            }

    val params =
        MapSqlParameterSource()
            .addValues(finalWrapper.params)
            .addValue("userId", userId.uuid)
            .addCategoryIds(categoryIdType, categoryIds)
    return jdbcTemplate.query(finalWrapper.sql, params) { rs, _ ->
      SpendingByCategory(
          month = rs.getDate("month").toLocalDate(),
          categoryName = rs.getString("category_name") ?: CategoryConstants.UNKNOWN_CATEGORY.name,
          amount = rs.getBigDecimal("amount"),
          color = rs.getString("color") ?: CategoryConstants.UNKNOWN_CATEGORY.color)
    }
  }

  private fun getSpendingByMonthCount(
      userId: TypedId<UserId>,
      categoryIdType: ReportCategoryIdFilterType,
      categoryIds: List<TypedId<CategoryId>>
  ): Long {
    val getSpendingByMonthCountSql =
        sqlLoader.loadSql("reports/get_total_spending_by_month_count.sql")
    val params =
        MapSqlParameterSource()
            .addValue("userId", userId.uuid)
            .addCategoryIds(categoryIdType, categoryIds)
    return jdbcTemplate.queryForObject(getSpendingByMonthCountSql, params, Long::class.java)!!
  }

  private data class SpendingByCategoryQueryWrapper(val params: Map<String, Any>, val sql: String)

  private fun getSpendingByMonth(
      userId: TypedId<UserId>,
      request: ReportRequest,
      categoryIdType: ReportCategoryIdFilterType,
      categoryIds: List<TypedId<CategoryId>>
  ): List<SpendingByMonth> {
    val getTotalSpendingByMonthSql = sqlLoader.loadSql("reports/get_total_spending_by_month.sql")
    val totalSpendingByMonthParams =
        MapSqlParameterSource()
            .addValue("userId", userId.uuid)
            .addValue("offset", request.pageNumber * request.pageSize)
            .addValue("limit", request.pageSize)
            .addCategoryIds(categoryIdType, categoryIds)
    return jdbcTemplate.query(getTotalSpendingByMonthSql, totalSpendingByMonthParams) { rs, _ ->
      SpendingByMonth(
          month = rs.getDate("month").toLocalDate(),
          total = rs.getBigDecimal("total"),
          categories = listOf())
    }
  }

  private fun MapSqlParameterSource.addCategoryIds(
      categoryIdType: ReportCategoryIdFilterType,
      categoryIds: List<TypedId<CategoryId>>
  ): MapSqlParameterSource {
    val (unknownCategoryIds, otherCategoryIds) =
        categoryIds.partition { categoryId -> CategoryConstants.UNKNOWN_CATEGORY.id == categoryId }
    val queryType =
        categoryIdType.toQueryType(unknownCategoryIds.isNotEmpty(), otherCategoryIds.isNotEmpty())

    val queryCategoryIds = otherCategoryIds.map { it.uuid }.ifEmpty { null }

    return this.addValue("categoryIdType", queryType.name).addValue("categoryIds", queryCategoryIds)
  }
}

private enum class ReportQueryCategoryFilterType {
  INCLUDE_NO_UNKNOWN,
  INCLUDE_WITH_UNKNOWN,
  EXCLUDE_NO_UNKNOWN,
  EXCLUDE_WITH_UNKNOWN,
  ALL_NO_UNKNOWN,
  ALL_WITH_UNKNOWN
}

private fun ReportCategoryIdFilterType.toQueryType(
    hasUnknownId: Boolean,
    hasOtherIds: Boolean
): ReportQueryCategoryFilterType =
    if (!hasOtherIds && hasUnknownId) {
      ReportQueryCategoryFilterType.ALL_WITH_UNKNOWN
    } else if (!hasOtherIds && !hasUnknownId) {
      ReportQueryCategoryFilterType.ALL_NO_UNKNOWN
    } else if (ReportCategoryIdFilterType.INCLUDE == this && hasUnknownId) {
      ReportQueryCategoryFilterType.INCLUDE_WITH_UNKNOWN
    } else if (ReportCategoryIdFilterType.INCLUDE == this && !hasUnknownId) {
      ReportQueryCategoryFilterType.INCLUDE_NO_UNKNOWN
    } else if (ReportCategoryIdFilterType.EXCLUDE == this && hasUnknownId) {
      ReportQueryCategoryFilterType.EXCLUDE_NO_UNKNOWN
    } else if (ReportCategoryIdFilterType.EXCLUDE == this && !hasUnknownId) {
      ReportQueryCategoryFilterType.EXCLUDE_WITH_UNKNOWN
    } else {
      throw IllegalArgumentException("Invalid combination of query filter values")
    }
