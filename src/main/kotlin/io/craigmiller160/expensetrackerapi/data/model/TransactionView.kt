package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJavaType
import io.craigmiller160.expensetrackerapi.data.model.core.ViewEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime
import org.hibernate.annotations.JavaType
import org.hibernate.annotations.JdbcType
import org.hibernate.type.descriptor.jdbc.UUIDJdbcType

@Entity
@Table(name = "transactions_view")
class TransactionView(
    @JavaType(TypedIdJavaType::class)
    @JdbcType(UUIDJdbcType::class)
    override var userId: TypedId<UserId>,
    override var expenseDate: LocalDate,
    override var description: String,
    override var amount: BigDecimal,
    var contentHash: String,
    @JavaType(TypedIdJavaType::class)
    @JdbcType(UUIDJdbcType::class)
    override var categoryId: TypedId<CategoryId>?,
    var categoryName: String?,
    override var confirmed: Boolean,
    var duplicate: Boolean,
    var created: ZonedDateTime,
    var updated: ZonedDateTime
) : ViewEntity<TransactionId>(), TransactionCommon
