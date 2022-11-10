package io.craigmiller160.expensetrackerapi.data.repository.impl

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import io.craigmiller160.expensetrackerapi.data.SqlLoader
import io.craigmiller160.expensetrackerapi.data.model.QTransactionView
import io.craigmiller160.expensetrackerapi.data.model.TransactionView
import io.craigmiller160.expensetrackerapi.data.projection.NeedsAttentionCount
import io.craigmiller160.expensetrackerapi.data.projection.NeedsAttentionOldest
import io.craigmiller160.expensetrackerapi.data.projection.NeedsAttentionType
import io.craigmiller160.expensetrackerapi.data.querydsl.QueryDSLSupport
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepositoryCustom
import io.craigmiller160.expensetrackerapi.web.types.transaction.SearchTransactionsRequest
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

private val needsAttentionOldestRowMapper: RowMapper<NeedsAttentionOldest> =
  RowMapper { rs, index ->
    NeedsAttentionOldest(
      NeedsAttentionType.valueOf(rs.getString("type")), rs.getDate("oldest")?.toLocalDate())
  }

@Repository
class TransactionRepositoryCustomImpl(
  private val queryFactory: JPAQueryFactory,
  private val queryDslSupport: QueryDSLSupport,
  private val jdbcTemplate: NamedParameterJdbcTemplate,
  private val sqlLoader: SqlLoader
) : TransactionRepositoryCustom {

  override fun getAllNeedsAttentionCounts(userId: Long): List<NeedsAttentionCount> {
    val countSql = sqlLoader.loadSql("get_all_needs_attention_counts.sql")
    val params = MapSqlParameterSource().addValue("userId", userId)
    return jdbcTemplate.query(countSql, params, needsAttentionCountRowMapper)
  }

  override fun getAllNeedsAttentionOldest(userId: Long): List<NeedsAttentionOldest> {
    val oldestSql = sqlLoader.loadSql("get_all_needs_attention_oldest.sql")
    val params = MapSqlParameterSource().addValue("userId", userId)
    return jdbcTemplate.query(oldestSql, params, needsAttentionOldestRowMapper)
  }

  override fun searchForTransactions(
    request: SearchTransactionsRequest,
    userId: Long,
    page: Pageable
  ): Page<TransactionView> {
    val whereClause =
      BooleanBuilder(QTransactionView.transactionView.userId.eq(userId))
        .let(
          QueryDSLSupport.andIfNotNull(request.startDate) {
            QTransactionView.transactionView.expenseDate.goe(it)
          })
        .let(
          QueryDSLSupport.andIfNotNull(request.endDate) {
            QTransactionView.transactionView.expenseDate.loe(it)
          })
        .let(
          QueryDSLSupport.andIfNotNull(request.isConfirmed) {
            QTransactionView.transactionView.confirmed.eq(it)
          })
        .let(
          QueryDSLSupport.andIfNotNull(request.isDuplicate) {
            QTransactionView.transactionView.duplicate.eq(it)
          })
        .let(
          QueryDSLSupport.andIfNotNull(request.categoryIds) {
            QTransactionView.transactionView.categoryId.`in`(it)
          })
        .let(
          QueryDSLSupport.andIfNotNull(request.isCategorized) {
            if (it) {
              QTransactionView.transactionView.categoryId.isNotNull
            } else {
              QTransactionView.transactionView.categoryId.isNull
            }
          })
        .let(
          QueryDSLSupport.andIfNotNull(request.isPossibleRefund) {
            if (it) {
              QTransactionView.transactionView.amount.gt(0)
            } else {
              QTransactionView.transactionView.amount.loe(0)
            }
          })

    val baseQuery = queryFactory.query().from(QTransactionView.transactionView).where(whereClause)

    val count = baseQuery.select(QTransactionView.transactionView.count()).fetchFirst()
    val results =
      baseQuery
        .select(QTransactionView.transactionView)
        .let(queryDslSupport.applyPagination(page, TransactionView::class.java))
        .fetch()

    return PageImpl(results, page, count)
  }
}
