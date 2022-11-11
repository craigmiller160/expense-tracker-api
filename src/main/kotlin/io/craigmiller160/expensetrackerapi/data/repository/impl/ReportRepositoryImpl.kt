package io.craigmiller160.expensetrackerapi.data.repository.impl

import io.craigmiller160.expensetrackerapi.data.SqlLoader
import io.craigmiller160.expensetrackerapi.data.projection.SpendingByCategory
import io.craigmiller160.expensetrackerapi.data.projection.SpendingByMonth
import io.craigmiller160.expensetrackerapi.data.repository.ReportRepository
import io.craigmiller160.expensetrackerapi.web.types.report.ReportRequest
import java.time.LocalDate
import javax.transaction.Transactional
import org.springframework.data.domain.Page
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
    val months = spendingByMonth.map { it.month }

    TODO("How to efficiently, functionally merge everything together without mutation?")
  }

  private fun getSpendingByCategoryForMonths(
    userId: Long,
    months: List<LocalDate>
  ): List<SpendingByCategory> {
    val getSpendingByCategoryForMonthSql =
      sqlLoader.loadSql("reports/get_spending_by_category_for_month.sql")
    TODO()
  }

  private fun getSpendingByMonth(userId: Long, request: ReportRequest): List<SpendingByMonth> {
    val getTotalSpendingByMonthSql = sqlLoader.loadSql("reports/get_total_spending_by_month.sql")
    val totalSpendingByMonthParams =
      MapSqlParameterSource()
        .addValue("userId", userId)
        .addValue("offset", request.pageNumber * request.pageSize)
        .addValue("limit", request.pageSize)
    return jdbcTemplate.query(getTotalSpendingByMonthSql, totalSpendingByMonthParams) { rs, i ->
      SpendingByMonth(
        month = rs.getDate("month").toLocalDate(),
        total = rs.getBigDecimal("total"),
        categories = listOf())
    }
  }
}
