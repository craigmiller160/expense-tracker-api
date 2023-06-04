package io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import jakarta.persistence.AttributeConverter
import java.util.UUID

class TypedIdConverter : AttributeConverter<TypedId<*>, UUID> {
  override fun convertToDatabaseColumn(attribute: TypedId<*>?): UUID? = attribute?.uuid

  override fun convertToEntityAttribute(dbData: UUID?): TypedId<*>? =
      dbData?.let { TypedId<Any>(it) }
}
