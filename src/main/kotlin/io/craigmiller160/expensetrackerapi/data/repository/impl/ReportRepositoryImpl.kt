package io.craigmiller160.expensetrackerapi.data.repository.impl

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.data.SqlLoader
import io.craigmiller160.expensetrackerapi.data.constants.CategoryConstants
import io.craigmiller160.expensetrackerapi.data.mustache.MustacheSqlTemplate
import io.craigmiller160.expensetrackerapi.data.projection.SpendingByCategory
import io.craigmiller160.expensetrackerapi.data.projection.SpendingByMonth
import io.craigmiller160.expensetrackerapi.data.repository.ReportRepository
import io.craigmiller160.expensetrackerapi.web.types.report.ReportCategoryIdFilterType
import io.craigmiller160.expensetrackerapi.web.types.report.ReportRequest
import java.time.LocalDate
import javax.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

private fun addCategoryIdsParam(
    excludeCategoryIds: List<TypedId<CategoryId>>
): (MapSqlParameterSource) -> MapSqlParameterSource = { params ->
  if (excludeCategoryIds.isNotEmpty())
      params.addValue("excludeCategoryIds", excludeCategoryIds.map { it.uuid })
  else params
}

// TODO update sql
private fun executeMustacheTemplate(
    categoryIdType: ReportCategoryIdFilterType,
    categoryIds: List<TypedId<CategoryId>>
): (MustacheSqlTemplate) -> String = { template ->
  if (categoryIds.isNotEmpty() && ReportCategoryIdFilterType.INCLUDE == categoryIdType) {
    template.executeWithParams("excludeCategoryIds")
  } else if (categoryIds.isNotEmpty() && ReportCategoryIdFilterType.EXCLUDE == categoryIdType) {
    template.executeWithParams("includeCategoryIds")
  } else {
    template.executeWithParams()
  }
}

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
      excludeCategoryIds: List<TypedId<CategoryId>>
  ): List<SpendingByCategory> {
    val getSpendingByCategoryForMonthSql =
        sqlLoader
            .loadSqlMustacheTemplate("reports/get_spending_by_category_for_month.sql")
            .let(executeMustacheTemplate(categoryIdType, excludeCategoryIds))
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
            .let(addCategoryIdsParam(excludeCategoryIds))
    return jdbcTemplate.query(finalWrapper.sql, params) { rs, _ ->
      SpendingByCategory(
          month = rs.getDate("month").toLocalDate(),
          categoryName = rs.getString("category_name") ?: CategoryConstants.UNKNOWN_CATEGORY_NAME,
          amount = rs.getBigDecimal("amount"),
          color = rs.getString("color") ?: CategoryConstants.UNKNOWN_CATEGORY_COLOR)
    }
  }

  private fun getSpendingByMonthCount(
      userId: TypedId<UserId>,
      categoryIdType: ReportCategoryIdFilterType,
      excludeCategoryIds: List<TypedId<CategoryId>>
  ): Long {
    val getSpendingByMonthCountSql =
        sqlLoader
            .loadSqlMustacheTemplate("reports/get_total_spending_by_month_count.sql")
            .let(executeMustacheTemplate(categoryIdType, excludeCategoryIds))
    val params =
        MapSqlParameterSource()
            .addValue("userId", userId.uuid)
            .let(addCategoryIdsParam(excludeCategoryIds))
    return jdbcTemplate.queryForObject(getSpendingByMonthCountSql, params, Long::class.java)!!
  }

  private data class SpendingByCategoryQueryWrapper(val params: Map<String, Any>, val sql: String)

  private fun getSpendingByMonth(
      userId: TypedId<UserId>,
      request: ReportRequest,
      categoryIdType: ReportCategoryIdFilterType,
      categoryIds: List<TypedId<CategoryId>>
  ): List<SpendingByMonth> {
    val getTotalSpendingByMonthSql =
        sqlLoader
            .loadSqlMustacheTemplate("reports/get_total_spending_by_month.sql")
            .let(executeMustacheTemplate(categoryIdType, categoryIds))
    val totalSpendingByMonthParams =
        MapSqlParameterSource()
            .addValue("userId", userId.uuid)
            .addValue("offset", request.pageNumber * request.pageSize)
            .addValue("limit", request.pageSize)
            .let(addCategoryIdsParam(categoryIds))
    return jdbcTemplate.query(getTotalSpendingByMonthSql, totalSpendingByMonthParams) { rs, _ ->
      SpendingByMonth(
          month = rs.getDate("month").toLocalDate(),
          total = rs.getBigDecimal("total"),
          categories = listOf())
    }
  }
}
