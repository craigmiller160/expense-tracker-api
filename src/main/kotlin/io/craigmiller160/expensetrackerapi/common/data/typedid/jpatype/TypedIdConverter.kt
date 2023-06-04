package io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.nio.ByteBuffer
import java.util.UUID

@Converter(autoApply = true)
class TypedIdConverter : AttributeConverter<TypedId<*>, UUID> {
  init {
    println("CREATING CONVERTER")
  }
  override fun convertToDatabaseColumn(attribute: TypedId<*>?): UUID? {
    println("CONVERTING TO DB")
    return attribute?.uuid
  }

  override fun convertToEntityAttribute(dbData: UUID?): TypedId<*>? {
    println("CONVERTING FROM DB")
    return dbData?.let { TypedId<Any>(it) }
  }
}

class TypedIdConverter2 : AttributeConverter<TypedId<*>, ByteArray> {
  override fun convertToDatabaseColumn(attribute: TypedId<*>?): ByteArray? =
      attribute
          ?.uuid
          ?.let { uuid ->
            ByteBuffer.wrap(ByteArray(16)).apply {
              putLong(uuid.mostSignificantBits)
              putLong(uuid.leastSignificantBits)
            }
          }
          ?.array()

  override fun convertToEntityAttribute(dbData: ByteArray?): TypedId<*>? =
      dbData
          ?.let { bytes ->
            val byteBuffer = ByteBuffer.wrap(bytes)
            val high = byteBuffer.getLong()
            val low = byteBuffer.getLong()
            UUID(high, low)
          }
          ?.let { TypedId<Any>(it) }
}
