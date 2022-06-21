package io.craigmiller160.expensetrackerapi.common.data.typedid.spring

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import org.springframework.core.convert.converter.Converter

class TypedIdSetConverter : Converter<String, Set<TypedId<*>>> {
  override fun convert(source: String): Set<TypedId<*>> =
      source.split(",").map { TypedId<Any>(it.trim()) }.toSet()
}
