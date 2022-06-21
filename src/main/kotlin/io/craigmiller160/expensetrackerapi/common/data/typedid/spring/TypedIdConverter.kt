package io.craigmiller160.expensetrackerapi.common.data.typedid.spring

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import org.springframework.core.convert.converter.Converter

class TypedIdConverter : Converter<String, TypedId<*>> {
  override fun convert(source: String): TypedId<*> = TypedId<Any>(source)
}
