package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import org.springframework.data.jpa.repository.JpaRepository

interface AutoCategorizeRuleRepository :
  JpaRepository<AutoCategorizeRule, TypedId<AutoCategorizeRuleId>>,
  AutoCategorizeRuleRepositoryCustom {
  fun countAllByUserId(userId: Long): Long

  fun findByIdAndUserId(id: TypedId<AutoCategorizeRuleId>, userId: Long): AutoCategorizeRule?

  fun deleteByIdAndUserId(id: TypedId<AutoCategorizeRuleId>, userId: Long)
}
