package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaType
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import org.hibernate.annotations.TypeDef

// TODO does this need a parent?
@Entity
@Table(name = "auto_categorize_rules_view")
@TypeDef(defaultForType = TypedId::class, typeClass = TypedIdJpaType::class)
data class AutoCategorizeRuleView(
  @Id val id: TypedId<AutoCategorizeRuleId>,
  val categoryId: TypedId<CategoryId>,
  val categoryName: String,
  val userId: Long,
  val ordinal: Int,
  val regex: String,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
  val minAmount: BigDecimal? = null,
  val maxAmount: BigDecimal? = null
)
