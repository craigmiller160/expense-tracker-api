package io.craigmiller160.expensetrackerapi.common.data.typeid.jpatype

import io.craigmiller160.expensetrackerapi.common.data.typeid.TypedId
import org.hibernate.type.descriptor.WrapperOptions
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor

class TypedIdDescriptor : AbstractTypeDescriptor<TypedId>(TypedId::class.java) {
  // TODO delete if unused
  companion object {
    val INSTANCE = TypedIdDescriptor()
  }
  override fun fromString(string: String): TypedId {
    TODO("Not yet implemented")
  }

  override fun <X : Any> wrap(value: X, options: WrapperOptions): TypedId {
    TODO("Not yet implemented")
  }

  override fun <X : Any> unwrap(value: TypedId, type: Class<X>, options: WrapperOptions): X {
    return UUIDTypeDescriptor.INSTANCE.unwrap(value.uuid, type, options)
  }
}
