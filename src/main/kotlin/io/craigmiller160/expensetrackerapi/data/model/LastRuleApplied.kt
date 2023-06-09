package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.LastRuleAppliedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJavaType
import io.craigmiller160.expensetrackerapi.data.model.core.ImmutableTableEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.JavaType
import org.hibernate.annotations.JdbcType
import org.hibernate.type.descriptor.jdbc.UUIDJdbcType

@Entity
@Table(name = "last_rule_applied")
class LastRuleApplied(
    @JavaType(TypedIdJavaType::class) @JdbcType(UUIDJdbcType::class) var userId: TypedId<UserId>,
    @JavaType(TypedIdJavaType::class)
    @JdbcType(UUIDJdbcType::class)
    var ruleId: TypedId<AutoCategorizeRuleId>,
    @JavaType(TypedIdJavaType::class)
    @JdbcType(UUIDJdbcType::class)
    var transactionId: TypedId<TransactionId>
) : ImmutableTableEntity<LastRuleAppliedId>()
