package io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype

import org.hibernate.annotations.JavaType
import org.hibernate.annotations.JdbcType
import org.hibernate.type.descriptor.jdbc.UUIDJdbcType

@JavaType(TypedIdJavaType::class)
@JdbcType(UUIDJdbcType::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class JpaTypedId
