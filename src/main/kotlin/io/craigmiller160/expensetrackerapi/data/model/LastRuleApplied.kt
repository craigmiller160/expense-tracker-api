package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.LastRuleAppliedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaType
import io.craigmiller160.expensetrackerapi.data.model.core.ImmutableEntity
import java.time.ZonedDateTime
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import org.hibernate.annotations.TypeDef

@Entity
@Table(name = "last_rule_applied")
@TypeDef(defaultForType = TypedId::class, typeClass = TypedIdJpaType::class)
data class LastRuleApplied(
  val ruleId: TypedId<AutoCategorizeRuleId>,
  val transactionId: TypedId<TransactionId>,
  @Id override val id: TypedId<LastRuleAppliedId> = TypedId(),
  override val created: ZonedDateTime = ZonedDateTime.now()
) : ImmutableEntity<LastRuleAppliedId>
