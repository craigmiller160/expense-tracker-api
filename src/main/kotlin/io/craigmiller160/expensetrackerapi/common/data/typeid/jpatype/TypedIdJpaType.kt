package io.craigmiller160.expensetrackerapi.common.data.typeid.jpatype

import io.craigmiller160.expensetrackerapi.common.data.typeid.TypedId
import org.hibernate.type.AbstractSingleColumnStandardBasicType
import org.hibernate.type.PostgresUUIDType.PostgresUUIDSqlTypeDescriptor

class TypedIdJpaType :
    AbstractSingleColumnStandardBasicType<TypedId>(
        PostgresUUIDSqlTypeDescriptor.INSTANCE, TypedIdDescriptor.INSTANCE) {
  // TODO delete if unused
  override fun getName(): String = TypedId::class.java.simpleName
  override fun registerUnderJavaType(): Boolean = true
}
