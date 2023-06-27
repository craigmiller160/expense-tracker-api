package io.craigmiller160.expensetrackerapi.data.repository.impl

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.data.model.QTransactionView
import io.craigmiller160.expensetrackerapi.data.model.TransactionView
import io.craigmiller160.expensetrackerapi.data.model.YesNoFilter
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
      userId: TypedId<UserId>,
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
                QueryDSLSupport.andYesNoFilter(
                    value = request.confirmed,
                    ifYes = QTransactionView.transactionView.confirmed.eq(true),
                    ifNo = QTransactionView.transactionView.confirmed.eq(false)))
            .let(
                QueryDSLSupport.andYesNoFilter(
                    value = request.duplicate,
                    ifYes = QTransactionView.transactionView.duplicate.eq(true),
                    ifNo = QTransactionView.transactionView.duplicate.eq(false)))
            .let(
                QueryDSLSupport.andIfNotNull(request.categoryIds) {
                  QTransactionView.transactionView.categoryId.`in`(it)
                })
            .let(
                QueryDSLSupport.andYesNoFilter(
                    value = request.categorized,
                    ifYes = QTransactionView.transactionView.categoryId.isNotNull,
                    ifNo = QTransactionView.transactionView.categoryId.isNull))
            .let(
                QueryDSLSupport.andYesNoFilter(
                    value = request.possibleRefund,
                    ifYes = QTransactionView.transactionView.amount.gt(0),
                    ifNo = QTransactionView.transactionView.amount.loe(0)))
            .let(
                QueryDSLSupport.andIfNotNull(request.description) {
                  QTransactionView.transactionView.description
                      .toLowerCase()
                      .like("%${it.lowercase()}%")
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

private fun yesNoFilter(
    value: YesNoFilter,
    ifYes: () -> BooleanExpression,
    ifNo: () -> BooleanExpression
): (BooleanBuilder) -> BooleanBuilder = { builder ->
  when (value) {
    YesNoFilter.YES -> ifYes()
    YesNoFilter.NO -> ifNo()
    YesNoFilter.ALL -> Expressions.TRUE
  }.let { builder.and(it) }
}
