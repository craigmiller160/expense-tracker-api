package io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import java.util.UUID
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class TypedIdConverter : AttributeConverter<TypedId<*>, UUID> {
  override fun convertToDatabaseColumn(attribute: TypedId<*>): UUID = attribute.uuid
  override fun convertToEntityAttribute(dbData: UUID): TypedId<*> = TypedId<Any>(dbData)
}
