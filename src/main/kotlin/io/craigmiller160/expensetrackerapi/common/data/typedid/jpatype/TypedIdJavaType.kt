package io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import org.hibernate.type.descriptor.WrapperOptions
import org.hibernate.type.descriptor.java.AbstractClassJavaType
import org.hibernate.type.descriptor.java.UUIDJavaType

class TypedIdJavaType private constructor() :
    AbstractClassJavaType<TypedId<*>>(TypedId::class.java) {
  companion object {
    @JvmStatic val INSTANCE = TypedIdJavaType()
  }
  override fun <X : Any?> unwrap(value: TypedId<*>?, type: Class<X>, options: WrapperOptions?): X =
      UUIDJavaType.INSTANCE.unwrap(value?.uuid, type, options)

  override fun <X : Any?> wrap(value: X, options: WrapperOptions?): TypedId<*> =
      TypedId<Any>(UUIDJavaType.INSTANCE.wrap(value, options))
}
