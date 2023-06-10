package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJavaType
import io.craigmiller160.expensetrackerapi.data.model.core.MutableTableEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate
import org.hibernate.annotations.JavaType
import org.hibernate.annotations.JdbcType
import org.hibernate.type.descriptor.jdbc.UUIDJdbcType

@Entity
@Table(name = "auto_categorize_rules")
class AutoCategorizeRule(
    @JavaType(TypedIdJavaType::class) @JdbcType(UUIDJdbcType::class) var userId: TypedId<UserId>,
    @JavaType(TypedIdJavaType::class)
    @JdbcType(UUIDJdbcType::class)
    var categoryId: TypedId<CategoryId>,
    var ordinal: Int,
    var regex: String,
    var startDate: LocalDate? = null,
    var endDate: LocalDate? = null,
    var minAmount: BigDecimal? = null,
    var maxAmount: BigDecimal? = null,
) : MutableTableEntity<AutoCategorizeRuleId>()
