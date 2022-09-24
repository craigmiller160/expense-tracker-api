package io.craigmiller160.expensetrackerapi.data.repository.impl

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.QTransaction
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.querydsl.QueryDSLSupport
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepositoryCustom
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsRequest
import javax.persistence.EntityManager
import javax.persistence.Query
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class TransactionRepositoryCustomImpl(
  private val entityManager: EntityManager,
  private val queryFactory: JPAQueryFactory,
  private val queryDslSupport: QueryDSLSupport
) : TransactionRepositoryCustom {
  companion object {
    private const val TEMP =
      """
          SELECT CASE
                WHEN :isCountQuery = true THEN COUNT(t)
                ELSE t
            END
      """
    private const val SEARCH_FOR_TRANSACTIONS =
      """
            SELECT t
            FROM Transaction t
            WHERE (COALESCE(:startDate, null) IS NULL OR :startDate >= t.expenseDate)
            AND (COALESCE(:endDate, null) IS NULL OR :endDate <= t.expenseDate)
            AND (COALESCE(:isConfirmed, null) IS NULL OR :isConfirmed = t.confirmed)
            AND (COALESCE(:isDuplicate, null) IS NULL OR :isDuplicate = t.duplicate)
            AND (COALESCE(:categories, null) IS NULL OR t.categoryId IN (:categories))
            AND (
                COALESCE(:isCategorized, null) IS NULL OR 
                (:isCategorized = true AND t.categoryId IS NOT NULL) OR
                (:isCategorized = false AND t.categoryId IS NULL)
            )
        """
  }
  override fun searchForTransactions2(
    request: SearchTransactionsRequest,
    categories: List<TypedId<CategoryId>>?,
    page: Pageable
  ): Page<Transaction> {
    // TODO all of this needs to be re-usable
    val count = 0L
    //      createBaseSearchForTransactionsQuery(request, categories)
    //        .setParameter("isCountQuery", true)
    //        .singleResult as Long
    val results =
      createBaseSearchForTransactionsQuery(request, categories)
        //        .setParameter("isCountQuery", false)
        .let {
          it.firstResult = page.pageNumber * page.pageSize
          it.maxResults = page.pageSize
          it
        }
        .resultList as List<Transaction>
    return PageImpl(results, page, count)
  }

  override fun searchForTransactions3(
    request: SearchTransactionsRequest,
    page: Pageable
  ): Page<Transaction> {
    val whereClause =
      BooleanBuilder()
        .let(
          queryDslSupport.andIfNotNull(request.startDate) {
            QTransaction.transaction.expenseDate.goe(it)
          })
        .let(
          queryDslSupport.andIfNotNull(request.endDate) {
            QTransaction.transaction.expenseDate.loe(it)
          })
        .let(
          queryDslSupport.andIfNotNull(request.isConfirmed) {
            QTransaction.transaction.confirmed.eq(it)
          })
        .let(
          queryDslSupport.andIfNotNull(request.isDuplicate) {
            QTransaction.transaction.duplicate.eq(it)
          })
        .let(
          queryDslSupport.andIfNotNull(request.categoryIds) {
            QTransaction.transaction.categoryId.`in`(it)
          })
        .let(
          queryDslSupport.andIfNotNull(request.isCategorized) {
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
        .let { queryDslSupport.applyPagination(it, page, Transaction::class.java) }
        .fetch()

    return PageImpl(results, page, count)
  }

  private fun createBaseSearchForTransactionsQuery(
    request: SearchTransactionsRequest,
    categories: List<TypedId<CategoryId>>?
  ): Query =
    entityManager
      .createQuery(SEARCH_FOR_TRANSACTIONS)
      .setParameter("startDate", request.startDate)
      .setParameter("endDate", request.endDate)
      .setParameter("isConfirmed", request.isConfirmed)
      .setParameter("isDuplicate", request.isDuplicate)
      .setParameter("categories", categories)
      .setParameter("isCategorized", request.isCategorized)
}
