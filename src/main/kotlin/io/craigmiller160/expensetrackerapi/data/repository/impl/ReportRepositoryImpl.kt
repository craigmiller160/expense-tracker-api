package io.craigmiller160.expensetrackerapi.data.repository.impl

import io.craigmiller160.expensetrackerapi.data.SqlLoader
import io.craigmiller160.expensetrackerapi.data.projection.SpendingByCategory
import io.craigmiller160.expensetrackerapi.data.projection.SpendingByMonth
import io.craigmiller160.expensetrackerapi.data.repository.ReportRepository
import io.craigmiller160.expensetrackerapi.web.types.report.ReportRequest
import java.time.LocalDate
import javax.transaction.Transactional
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
    userId: Long,
    request: ReportRequest
  ): Page<SpendingByMonth> {
    val spendingByMonth = getSpendingByMonth(userId, request)
    val spendingByMonthCount = getSpendingByMonthCount(userId)
    val months = spendingByMonth.map { it.month }
    val spendingByCategory = getSpendingByCategoryForMonths(userId, months)
    val fullResults =
      spendingByMonth.map { monthRecord ->
        monthRecord.copy(
          categories =
            // The records should already be in the correct order
            spendingByCategory.filter { categoryRecord ->
              categoryRecord.month == monthRecord.month
            })
      }

    return PageImpl(
      fullResults, PageRequest.of(request.pageNumber, request.pageSize), spendingByMonthCount)
  }

  private fun getSpendingByCategoryForMonths(
    userId: Long,
    months: List<LocalDate>
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

    val params = MapSqlParameterSource().addValues(finalWrapper.params).addValue("userId", userId)
    return jdbcTemplate.query(finalWrapper.sql, params) { rs, _ ->
      SpendingByCategory(
        month = rs.getDate("month").toLocalDate(),
        categoryName = rs.getString("category_name") ?: "Unknown",
        amount = rs.getBigDecimal("amount"))
    }
  }

  private fun getSpendingByMonthCount(userId: Long): Long {
    val getSpendingByMonthCountSql =
      sqlLoader.loadSql("reports/get_total_spending_by_month_count.sql")
    val params = MapSqlParameterSource().addValue("userId", userId)
    return jdbcTemplate.queryForObject(getSpendingByMonthCountSql, params, Long::class.java)!!
  }

  private data class SpendingByCategoryQueryWrapper(val params: Map<String, Any>, val sql: String)

  private fun getSpendingByMonth(userId: Long, request: ReportRequest): List<SpendingByMonth> {
    val getTotalSpendingByMonthSql = sqlLoader.loadSql("reports/get_total_spending_by_month.sql")
    val totalSpendingByMonthParams =
      MapSqlParameterSource()
        .addValue("userId", userId)
        .addValue("offset", request.pageNumber * request.pageSize)
        .addValue("limit", request.pageSize)
    return jdbcTemplate.query(getTotalSpendingByMonthSql, totalSpendingByMonthParams) { rs, _ ->
      SpendingByMonth(
        month = rs.getDate("month").toLocalDate(),
        total = rs.getBigDecimal("total"),
        categories = listOf())
    }
  }
}
