package io.craigmiller160.expensetrackerapi.data.repository.impl

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import io.craigmiller160.expensetrackerapi.data.model.QTransactionView
import io.craigmiller160.expensetrackerapi.data.model.TransactionView
import io.craigmiller160.expensetrackerapi.data.querydsl.QueryDSLSupport
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepositoryCustom
import io.craigmiller160.expensetrackerapi.web.types.transaction.SearchTransactionsRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class TransactionRepositoryCustomImpl(
  private val queryFactory: JPAQueryFactory,
  private val queryDslSupport: QueryDSLSupport
) : TransactionRepositoryCustom {

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
