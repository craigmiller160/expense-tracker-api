package io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import org.hibernate.type.descriptor.WrapperOptions
import org.hibernate.type.descriptor.java.AbstractClassJavaType
import org.hibernate.type.descriptor.java.UUIDJavaType

class TypedIdJavaType : AbstractClassJavaType<TypedId<*>>(TypedId::class.java) {
  override fun <X : Any?> unwrap(value: TypedId<*>?, type: Class<X>, options: WrapperOptions?): X? =
      value?.let { UUIDJavaType.INSTANCE.unwrap(it.uuid, type, options) }

  override fun <X : Any?> wrap(value: X?, options: WrapperOptions?): TypedId<*>? =
      value?.let { TypedId<Any>(UUIDJavaType.INSTANCE.wrap(it, options)) }
}
