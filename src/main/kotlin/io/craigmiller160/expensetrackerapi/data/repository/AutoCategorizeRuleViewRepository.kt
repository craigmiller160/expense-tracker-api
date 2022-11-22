package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRuleView
import org.springframework.data.jpa.repository.JpaRepository

interface AutoCategorizeRuleViewRepository :
  JpaRepository<AutoCategorizeRuleView, TypedId<AutoCategorizeRuleId>>,
  AutoCategorizeRuleViewRepositoryCustom
