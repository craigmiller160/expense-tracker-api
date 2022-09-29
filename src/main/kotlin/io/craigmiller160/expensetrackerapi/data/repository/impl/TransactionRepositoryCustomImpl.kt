package io.craigmiller160.expensetrackerapi.data.repository.impl

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import io.craigmiller160.expensetrackerapi.data.SqlLoader
import io.craigmiller160.expensetrackerapi.data.model.QTransaction
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.projection.NeedsAttentionCount
import io.craigmiller160.expensetrackerapi.data.projection.NeedsAttentionType
import io.craigmiller160.expensetrackerapi.data.querydsl.QueryDSLSupport
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepositoryCustom
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

private val needsAttentionCountRowMapper: RowMapper<NeedsAttentionCount> = RowMapper { rs, index ->
  NeedsAttentionCount(NeedsAttentionType.valueOf(rs.getString("type")), rs.getLong("count"))
}

@Repository
class TransactionRepositoryCustomImpl(
  private val queryFactory: JPAQueryFactory,
  private val queryDslSupport: QueryDSLSupport,
  private val jdbcTemplate: NamedParameterJdbcTemplate,
  private val sqlLoader: SqlLoader
) : TransactionRepositoryCustom {

  override fun getAllNeedsAttentionCounts(userId: Long): List<NeedsAttentionCount> {
    val sql = sqlLoader.loadSql("get_all_needs_attention.sql")
    val params = MapSqlParameterSource().addValue("userId", userId)
    val needsAttentionCounts =
      jdbcTemplate.query(sql, params, needsAttentionCountRowMapper).associateBy { it.type }
    TODO("Not yet implemented")
  }

  override fun searchForTransactions(
    request: SearchTransactionsRequest,
    userId: Long,
    page: Pageable
  ): Page<Transaction> {
    val whereClause =
      BooleanBuilder(QTransaction.transaction.userId.eq(userId))
        .let(
          QueryDSLSupport.andIfNotNull(request.startDate) {
            QTransaction.transaction.expenseDate.goe(it)
          })
        .let(
          QueryDSLSupport.andIfNotNull(request.endDate) {
            QTransaction.transaction.expenseDate.loe(it)
          })
        .let(
          QueryDSLSupport.andIfNotNull(request.isConfirmed) {
            QTransaction.transaction.confirmed.eq(it)
          })
        .let(
          QueryDSLSupport.andIfNotNull(request.isDuplicate) {
            QTransaction.transaction.duplicate.eq(it)
          })
        .let(
          QueryDSLSupport.andIfNotNull(request.categoryIds) {
            QTransaction.transaction.categoryId.`in`(it)
          })
        .let(
          QueryDSLSupport.andIfNotNull(request.isCategorized) {
            if (it) {
              QTransaction.transaction.categoryId.isNotNull
            } else {
              QTransaction.transaction.categoryId.isNull
            }
          })

    val baseQuery = queryFactory.query().from(QTransaction.transaction).where(whereClause)

    val count = baseQuery.select(QTransaction.transaction.count()).fetchFirst()
    val results =
      baseQuery
        .select(QTransaction.transaction)
        .let(queryDslSupport.applyPagination(page, Transaction::class.java))
        .fetch()

    return PageImpl(results, page, count)
  }
}
