package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.data.model.core.ViewEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "auto_categorize_rules_view")
class AutoCategorizeRuleView(
    var categoryId: TypedId<CategoryId>,
    var categoryName: String,
    var userId: TypedId<UserId>,
    var ordinal: Int,
    var regex: String,
    var startDate: LocalDate? = null,
    var endDate: LocalDate? = null,
    var minAmount: BigDecimal? = null,
    var maxAmount: BigDecimal? = null
) : ViewEntity<AutoCategorizeRuleId>()
