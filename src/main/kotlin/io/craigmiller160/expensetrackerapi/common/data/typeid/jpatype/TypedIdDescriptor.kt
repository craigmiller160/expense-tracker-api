package io.craigmiller160.expensetrackerapi.common.data.typeid.jpatype

import io.craigmiller160.expensetrackerapi.common.data.typeid.TypedId
import java.util.UUID
import org.hibernate.type.descriptor.WrapperOptions
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor

class TypedIdDescriptor : AbstractTypeDescriptor<TypedId<*>>(TypedId::class.java) {
  companion object {
    val INSTANCE = TypedIdDescriptor()
  }
  override fun fromString(string: String): TypedId<*> = TypedId<Any>(string)

  override fun <X : Any> wrap(value: X?, options: WrapperOptions): TypedId<*>? =
      value?.let { nonNullValue ->
        when (nonNullValue) {
          is ByteArray ->
              TypedId(UUIDTypeDescriptor.ToBytesTransformer.INSTANCE.parse(nonNullValue))
          is String ->
              TypedId<Any>(UUIDTypeDescriptor.ToStringTransformer.INSTANCE.parse(nonNullValue))
          is UUID -> TypedId<Any>(nonNullValue)
          else -> throw unknownWrap(nonNullValue::class.java)
        }
      }

  override fun <X : Any> unwrap(value: TypedId<*>, type: Class<X>, options: WrapperOptions): X =
      UUIDTypeDescriptor.INSTANCE.unwrap(value.uuid, type, options)
}
