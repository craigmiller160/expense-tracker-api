package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaType
import io.craigmiller160.expensetrackerapi.data.model.core.MutableLegacyEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version
import org.hibernate.annotations.TypeDef

// TODO refactor
@Entity
@Table(name = "auto_categorize_rules")
@TypeDef(defaultForType = TypedId::class, typeClass = TypedIdJpaType::class)
data class AutoCategorizeRule(
  val userId: Long,
  val categoryId: TypedId<CategoryId>,
  val ordinal: Int,
  val regex: String,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
  val minAmount: BigDecimal? = null,
  val maxAmount: BigDecimal? = null,
  @Id override val id: TypedId<AutoCategorizeRuleId> = TypedId(),
  override val created: ZonedDateTime = ZonedDateTime.now(),
  override var updated: ZonedDateTime = ZonedDateTime.now(),
  @Version override val version: Long = 1
) : MutableLegacyEntity<AutoCategorizeRuleId>
