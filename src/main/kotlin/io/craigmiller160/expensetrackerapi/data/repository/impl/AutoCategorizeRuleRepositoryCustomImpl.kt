package io.craigmiller160.expensetrackerapi.data.repository.impl

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.JPQLQueryFactory
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.model.QAutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.querydsl.QueryDSLSupport
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepositoryCustom
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

@Repository
class AutoCategorizeRuleRepositoryCustomImpl(
  private val queryFactory: JPQLQueryFactory,
  private val queryDslSupport: QueryDSLSupport
) : AutoCategorizeRuleRepositoryCustom {
  override fun searchForRules(
    request: AutoCategorizeRulePageRequest,
    userId: Long
  ): Page<AutoCategorizeRule> {
    val sort = Sort.by(Sort.Order.asc("ordinal"))
    val pageable = PageRequest.of(request.pageNumber, request.pageSize, sort)

    val whereClause =
      BooleanBuilder(QAutoCategorizeRule.autoCategorizeRule.userId.eq(userId))
        .let(
          QueryDSLSupport.andIfNotNull(request.categoryId) {
            QAutoCategorizeRule.autoCategorizeRule.categoryId.eq(it)
          })
        .let(
          QueryDSLSupport.andIfNotNull(request.regex) {
            QAutoCategorizeRule.autoCategorizeRule.regex.toLowerCase().like("%${it.lowercase()}%")
          })

    val baseQuery =
      queryFactory.query().from(QAutoCategorizeRule.autoCategorizeRule).where(whereClause)

    val count = baseQuery.select(QAutoCategorizeRule.autoCategorizeRule.count()).fetchFirst()
    val results =
      baseQuery
        .select(QAutoCategorizeRule.autoCategorizeRule)
        .let(queryDslSupport.applyPagination(pageable, AutoCategorizeRule::class.java))
        .fetch()

    return PageImpl(results, pageable, count)
  }

  override fun decrementOrdinals(
    userId: Long,
    minOrdinal: Int,
    maxOrdinal: Int,
    excludeId: TypedId<AutoCategorizeRuleId>?
  ) {
    TODO("Not yet implemented")
  }

  override fun incrementOrdinals(
    userId: Long,
    minOrdinal: Int,
    maxOrdinal: Int,
    excludeId: TypedId<AutoCategorizeRuleId>?
  ) {
    TODO("Not yet implemented")
  }
}
