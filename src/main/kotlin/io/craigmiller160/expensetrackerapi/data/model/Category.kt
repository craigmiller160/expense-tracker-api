package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJavaType
import io.craigmiller160.expensetrackerapi.data.model.core.MutableTableEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.JavaType
import org.hibernate.annotations.JdbcType
import org.hibernate.type.descriptor.jdbc.UUIDJdbcType

@Entity
@Table(name = "categories")
class Category(
    var name: String,
    @JavaType(TypedIdJavaType::class) @JdbcType(UUIDJdbcType::class) var userId: TypedId<UserId>,
    var color: String
) : MutableTableEntity<CategoryId>()
