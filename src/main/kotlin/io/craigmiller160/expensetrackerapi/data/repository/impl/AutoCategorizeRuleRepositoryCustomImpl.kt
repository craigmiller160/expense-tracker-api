package io.craigmiller160.expensetrackerapi.data.repository.impl

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.JPQLQueryFactory
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.model.QAutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.querydsl.QueryDSLSupport
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepositoryCustom
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
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
    val pageable = PageRequest.of(request.pageNumber, request.pageSize)

    val whereClause =
      BooleanBuilder(QAutoCategorizeRule.autoCategorizeRule.userId.eq(userId))
        .let(
          QueryDSLSupport.andIfNotNull(request.categoryId) {
            QAutoCategorizeRule.autoCategorizeRule.categoryId.eq(request.categoryId)
          })

    val baseQuery =
      queryFactory.query().from(QAutoCategorizeRule.autoCategorizeRule).where(whereClause)

    TODO("Not yet implemented")
  }
}
