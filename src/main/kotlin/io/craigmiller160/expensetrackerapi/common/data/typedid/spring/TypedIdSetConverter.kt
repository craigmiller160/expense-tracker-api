package io.craigmiller160.expensetrackerapi.common.data.typedid.spring

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import org.springframework.core.convert.converter.Converter

class TypedIdSetConverter : Converter<Set<String>, Set<TypedId<*>>> {
  override fun convert(source: Set<String>): Set<TypedId<*>> =
      source.map { TypedId<Any>(it) }.toSet()
}
