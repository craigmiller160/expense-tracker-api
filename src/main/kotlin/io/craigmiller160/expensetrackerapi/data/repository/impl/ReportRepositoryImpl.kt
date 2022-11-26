package io.craigmiller160.expensetrackerapi.data.repository.impl

import io.craigmiller160.expensetrackerapi.data.SqlLoader
import io.craigmiller160.expensetrackerapi.data.projection.SpendingByCategory
import io.craigmiller160.expensetrackerapi.data.projection.SpendingByMonth
import io.craigmiller160.expensetrackerapi.data.repository.ReportRepository
import io.craigmiller160.expensetrackerapi.web.types.report.ReportRequest
import java.time.LocalDate
import java.util.UUID
import javax.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

private fun addExcludeCategoryIdsParam(
  excludeCategoryIds: List<UUID>
): (MapSqlParameterSource) -> MapSqlParameterSource = { params ->
  val paramValue = excludeCategoryIds.ifEmpty { "" }
  params.addValue("excludeCategoryIds", paramValue)
}

@Repository
class ReportRepositoryImpl(
  private val jdbcTemplate: NamedParameterJdbcTemplate,
  private val sqlLoader: SqlLoader
) : ReportRepository {
  @Transactional
  override fun getSpendingByMonthAndCategory(
    userId: Long,
    request: ReportRequest
  ): Page<SpendingByMonth> {
    val categoryUUIDs = request.excludeCategoryIds.map { it.uuid }
    val spendingByMonth = getSpendingByMonth(userId, request, categoryUUIDs)
    val spendingByMonthCount = getSpendingByMonthCount(userId, categoryUUIDs)
    val months = spendingByMonth.map { it.month }

    val fullResults =
      if (months.isNotEmpty()) {
        val spendingByCategory = getSpendingByCategoryForMonths(userId, months, categoryUUIDs)
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
    userId: Long,
    months: List<LocalDate>,
    excludeCategoryIds: List<UUID>
  ): List<SpendingByCategory> {
    val getSpendingByCategoryForMonthSql =
      sqlLoader.loadSql("reports/get_spending_by_category_for_month.sql")
    val finalWrapper =
      months
        .mapIndexed { index, month ->
          val sql =
            getSpendingByCategoryForMonthSql.replace(":theDate", ":theDate$index").replace(";", "")
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
        .addValue("userId", userId)
        .let(addExcludeCategoryIdsParam(excludeCategoryIds))
    return jdbcTemplate.query(finalWrapper.sql, params) { rs, _ ->
      SpendingByCategory(
        month = rs.getDate("month").toLocalDate(),
        categoryName = rs.getString("category_name") ?: ReportRepository.UNKNOWN_CATEGORY_NAME,
        amount = rs.getBigDecimal("amount"),
        color = rs.getString("color") ?: ReportRepository.UNKNOWN_CATEGORY_COLOR)
    }
  }

  private fun getSpendingByMonthCount(userId: Long, excludeCategoryIds: List<UUID>): Long {
    val getSpendingByMonthCountSql =
      sqlLoader.loadSql("reports/get_total_spending_by_month_count.sql")
    val params =
      MapSqlParameterSource()
        .addValue("userId", userId)
        .let(addExcludeCategoryIdsParam(excludeCategoryIds))
    return jdbcTemplate.queryForObject(getSpendingByMonthCountSql, params, Long::class.java)!!
  }

  private data class SpendingByCategoryQueryWrapper(val params: Map<String, Any>, val sql: String)

  private fun getSpendingByMonth(
    userId: Long,
    request: ReportRequest,
    excludeCategoryIds: List<UUID>
  ): List<SpendingByMonth> {
    val getTotalSpendingByMonthSql = sqlLoader.loadSql("reports/get_total_spending_by_month.sql")
    val totalSpendingByMonthParams =
      MapSqlParameterSource()
        .addValue("userId", userId)
        .addValue("offset", request.pageNumber * request.pageSize)
        .addValue("limit", request.pageSize)
        .let(addExcludeCategoryIdsParam(excludeCategoryIds))
    return jdbcTemplate.query(getTotalSpendingByMonthSql, totalSpendingByMonthParams) { rs, _ ->
      SpendingByMonth(
        month = rs.getDate("month").toLocalDate(),
        total = rs.getBigDecimal("total"),
        categories = listOf())
    }
  }
}
