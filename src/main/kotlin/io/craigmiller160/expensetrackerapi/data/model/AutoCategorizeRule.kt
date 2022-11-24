package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.core.MutableEntity
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "auto_categorize_rules")
class AutoCategorizeRule(
  var userId: Long,
  var categoryId: TypedId<CategoryId>,
  var ordinal: Int,
  var regex: String,
  var startDate: LocalDate? = null,
  var endDate: LocalDate? = null,
  var minAmount: BigDecimal? = null,
  var maxAmount: BigDecimal? = null,
) : MutableEntity<AutoCategorizeRuleId>()
