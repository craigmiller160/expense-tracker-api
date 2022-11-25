package io.craigmiller160.expensetrackerapi.data.repository.impl

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.JPQLQueryFactory
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.data.model.QAutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.querydsl.QueryDSLSupport
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepositoryCustom
import org.springframework.stereotype.Repository

@Repository
class AutoCategorizeRuleRepositoryCustomImpl(
  private val queryFactory: JPQLQueryFactory,
  private val queryDslSupport: QueryDSLSupport
) : AutoCategorizeRuleRepositoryCustom {

  override fun decrementOrdinals(
    userId: Long,
    minOrdinal: Int,
    maxOrdinal: Int,
    excludeId: TypedId<AutoCategorizeRuleId>?
  ) {
    val whereClause = createOrdinalUpdateWhereClause(userId, minOrdinal, maxOrdinal, excludeId)
    queryFactory
      .update(QAutoCategorizeRule.autoCategorizeRule)
      .set(
        QAutoCategorizeRule.autoCategorizeRule.ordinal,
        QAutoCategorizeRule.autoCategorizeRule.ordinal.subtract(1))
      .set(
        QAutoCategorizeRule.autoCategorizeRule.version,
        QAutoCategorizeRule.autoCategorizeRule.version.add(1))
      .where(whereClause)
      .execute()
  }

  private fun createOrdinalUpdateWhereClause(
    userId: Long,
    minOrdinal: Int,
    maxOrdinal: Int,
    excludeId: TypedId<AutoCategorizeRuleId>?
  ): BooleanBuilder =
    BooleanBuilder(QAutoCategorizeRule.autoCategorizeRule.userId.eq(userId))
      .and(QAutoCategorizeRule.autoCategorizeRule.ordinal.loe(maxOrdinal))
      .and(QAutoCategorizeRule.autoCategorizeRule.ordinal.goe(minOrdinal))
      .let(
        QueryDSLSupport.andIfNotNull(excludeId) { id ->
          QAutoCategorizeRule.autoCategorizeRule.uid.ne(id)
        })

  override fun incrementOrdinals(
    userId: Long,
    minOrdinal: Int,
    maxOrdinal: Int,
    excludeId: TypedId<AutoCategorizeRuleId>?
  ) {
    val whereClause = createOrdinalUpdateWhereClause(userId, minOrdinal, maxOrdinal, excludeId)
    queryFactory
      .update(QAutoCategorizeRule.autoCategorizeRule)
      .set(
        QAutoCategorizeRule.autoCategorizeRule.ordinal,
        QAutoCategorizeRule.autoCategorizeRule.ordinal.add(1))
      .set(
        QAutoCategorizeRule.autoCategorizeRule.version,
        QAutoCategorizeRule.autoCategorizeRule.version.add(1))
      .where(whereClause)
      .execute()
  }
}
