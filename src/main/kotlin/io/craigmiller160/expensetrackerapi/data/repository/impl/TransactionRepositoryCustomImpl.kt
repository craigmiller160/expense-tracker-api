package io.craigmiller160.expensetrackerapi.data.repository.impl

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepositoryCustom
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsRequest
import javax.persistence.EntityManager
import javax.persistence.Query
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class TransactionRepositoryCustomImpl(private val entityManager: EntityManager) :
  TransactionRepositoryCustom {
  companion object {
    private const val SEARCH_FOR_TRANSACTIONS =
      """
            SELECT CASE
                WHEN :isCountQuery = true THEN COUNT(t)
                ELSE t
            END
            FROM Transaction t
            WHERE (:startDate IS NULL OR :startDate >= t.expenseDate)
            AND (:endDate IS NULL OR :endDate <= t.expenseDate)
            AND (:isConfirmed IS NULL OR :isConfirmed = t.confirmed)
            AND (:isDuplicate IS NULL OR :isDuplicate = t.duplicate)
            AND (:categories IS NULL OR t.categoryId IN (:categories))
            AND (
                :isCategorized IS NULL OR 
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
    val count =
      createBaseSearchForTransactionsQuery(request, categories)
        .setParameter("isCountQuery", true)
        .singleResult as Long
    val results =
      createBaseSearchForTransactionsQuery(request, categories)
        .setParameter("isCountQuery", false)
        .let {
          it.firstResult = page.pageNumber * page.pageSize
          it.maxResults = page.pageSize
          it
        }
        .resultList as List<Transaction>
    return PageImpl(results, page, count)
  }

  private fun createBaseSearchForTransactionsQuery(
    request: SearchTransactionsRequest,
    categories: List<TypedId<CategoryId>>?
  ): Query =
    entityManager
      .createQuery(SEARCH_FOR_TRANSACTIONS, Transaction::class.java)
      .setParameter("startDate", request.startDate)
      .setParameter("endDate", request.endDate)
      .setParameter("isConfirmed", request.isConfirmed)
      .setParameter("isDuplicate", request.isDuplicate)
      .setParameter("categories", categories)
      .setParameter("isCategorized", request.isCategorized)
}
