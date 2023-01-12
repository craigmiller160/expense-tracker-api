package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.core.ViewEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "auto_categorize_rules_view")
class AutoCategorizeRuleView(
  var categoryId: TypedId<CategoryId>,
  var categoryName: String,
  var userId: UUID,
  var ordinal: Int,
  var regex: String,
  var startDate: LocalDate? = null,
  var endDate: LocalDate? = null,
  var minAmount: BigDecimal? = null,
  var maxAmount: BigDecimal? = null
) : ViewEntity<AutoCategorizeRuleId>()
