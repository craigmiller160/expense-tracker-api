package io.craigmiller160.expensetrackerapi.data.repository.impl

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.JPQLQueryFactory
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRuleView
import io.craigmiller160.expensetrackerapi.data.model.QAutoCategorizeRuleView
import io.craigmiller160.expensetrackerapi.data.querydsl.QueryDSLSupport
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleViewRepositoryCustom
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

@Repository
class AutoCategorizeRuleViewRepositoryCustomImpl(
    private val queryFactory: JPQLQueryFactory,
    private val queryDslSupport: QueryDSLSupport
) : AutoCategorizeRuleViewRepositoryCustom {
  override fun searchForRules(
      request: AutoCategorizeRulePageRequest,
      userId: TypedId<UserId>
  ): Page<AutoCategorizeRuleView> {
    val sort = Sort.by(Sort.Order.asc("ordinal"))
    val pageable = PageRequest.of(request.pageNumber, request.pageSize, sort)

    val whereClause =
        BooleanBuilder(QAutoCategorizeRuleView.autoCategorizeRuleView.userId.eq(userId))
            .let(
                QueryDSLSupport.andIfNotNull(request.categoryId) {
                  QAutoCategorizeRuleView.autoCategorizeRuleView.categoryId.eq(it)
                })
            .let(
                QueryDSLSupport.andIfNotNull(request.regex) {
                  QAutoCategorizeRuleView.autoCategorizeRuleView.regex
                      .toLowerCase()
                      .like("%${it.lowercase()}%")
                })

    val baseQuery =
        queryFactory.query().from(QAutoCategorizeRuleView.autoCategorizeRuleView).where(whereClause)

    val count =
        baseQuery.select(QAutoCategorizeRuleView.autoCategorizeRuleView.count()).fetchFirst()
    val results =
        baseQuery
            .select(QAutoCategorizeRuleView.autoCategorizeRuleView)
            .let(queryDslSupport.applyPagination(pageable, AutoCategorizeRuleView::class.java))
            .fetch()

    return PageImpl(results, pageable, count)
  }
}
