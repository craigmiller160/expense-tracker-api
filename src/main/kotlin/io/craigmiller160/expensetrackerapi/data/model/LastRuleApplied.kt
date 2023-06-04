package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.LastRuleAppliedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.data.model.core.ImmutableTableEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "last_rule_applied")
class LastRuleApplied(
    var userId: TypedId<UserId>,
    var ruleId: TypedId<AutoCategorizeRuleId>,
    var transactionId: TypedId<TransactionId>
) : ImmutableTableEntity<LastRuleAppliedId>()
