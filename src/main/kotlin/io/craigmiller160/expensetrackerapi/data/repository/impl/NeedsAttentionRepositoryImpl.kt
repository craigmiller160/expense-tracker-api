package io.craigmiller160.expensetrackerapi.data.repository.impl

import io.craigmiller160.expensetrackerapi.data.SqlLoader
import io.craigmiller160.expensetrackerapi.data.projection.NeedsAttentionCount
import io.craigmiller160.expensetrackerapi.data.projection.NeedsAttentionOldest
import io.craigmiller160.expensetrackerapi.data.projection.NeedsAttentionType
import io.craigmiller160.expensetrackerapi.data.repository.NeedsAttentionRepository
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

private val needsAttentionCountRowMapper: RowMapper<NeedsAttentionCount> = RowMapper { rs, _ ->
  NeedsAttentionCount(NeedsAttentionType.valueOf(rs.getString("type")), rs.getLong("count"))
}

private val needsAttentionOldestRowMapper: RowMapper<NeedsAttentionOldest> = RowMapper { rs, _ ->
  NeedsAttentionOldest(
    NeedsAttentionType.valueOf(rs.getString("type")), rs.getDate("oldest")?.toLocalDate())
}

@Repository
class NeedsAttentionRepositoryImpl(
  private val jdbcTemplate: NamedParameterJdbcTemplate,
  private val sqlLoader: SqlLoader
) : NeedsAttentionRepository {
  override fun getAllNeedsAttentionCounts(userId: Long): List<NeedsAttentionCount> {
    val countSql = sqlLoader.loadSql("needsAttention/get_all_needs_attention_counts.sql")
    val params = MapSqlParameterSource().addValue("userId", userId)
    return jdbcTemplate.query(countSql, params, needsAttentionCountRowMapper)
  }

  override fun getAllNeedsAttentionOldest(userId: Long): List<NeedsAttentionOldest> {
    val oldestSql = sqlLoader.loadSql("needsAttention/get_all_needs_attention_oldest.sql")
    val params = MapSqlParameterSource().addValue("userId", userId)
    return jdbcTemplate.query(oldestSql, params, needsAttentionOldestRowMapper)
  }
}
