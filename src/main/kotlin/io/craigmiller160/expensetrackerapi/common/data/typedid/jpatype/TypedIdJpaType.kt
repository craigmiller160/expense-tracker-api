package io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.internal.util.IndexedConsumer
import org.hibernate.metamodel.mapping.Bindable
import org.hibernate.type.AbstractStandardBasicType
import org.hibernate.type.descriptor.jdbc.UUIDJdbcType

class TypedIdJpaType :
    AbstractStandardBasicType<TypedId<*>>(UUIDJdbcType.INSTANCE, TypedIdJavaType.INSTANCE) {
  override fun getName(): String = "typed-id"

  override fun forEachJdbcType(offset: Int, action: IndexedConsumer<*>?): Int {
    super.forEachJdbcType(offset, action)
    TODO("Not yet implemented")
  }

  override fun <X : Any?, Y : Any?> forEachDisassembledJdbcValue(
      value: Any?,
      offset: Int,
      x: Any?,
      y: Any?,
      valuesConsumer: Bindable.JdbcValuesBiConsumer<*, *>?,
      session: SharedSessionContractImplementor?
  ): Int {
    TODO("Not yet implemented")
  }
}
