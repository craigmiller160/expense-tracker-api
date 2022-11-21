package io.craigmiller160.expensetrackerapi.data.repository.impl

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.SqlLoader
import io.craigmiller160.expensetrackerapi.data.projection.LastRuleAppliedForTransaction
import io.craigmiller160.expensetrackerapi.data.repository.LastRuleAppliedRepositoryCustom
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class LastRuleAppliedRepositoryCustomImpl(
  private val jdbcTemplate: NamedParameterJdbcTemplate,
  private val sqlLoader: SqlLoader
) : LastRuleAppliedRepositoryCustom {
  override fun getLastRuleDetailsForTransaction(
    userId: Long,
    transactionId: TypedId<TransactionId>
  ): LastRuleAppliedForTransaction? {
    val params =
      MapSqlParameterSource().addValue("userId", userId).addValue("transactionId", transactionId)
    val sql = sqlLoader.loadSql("lastRuleApplied/getLastRuleAppliedForTransaction.sql")
    val results = jdbcTemplate.query(sql, params) { rs, _ -> LastRuleAppliedForTransaction() }

    return if (results.isNotEmpty()) results.first() else null
  }
}
