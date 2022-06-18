package io.craigmiller160.expensetrackerapi.common.data.typedid.jackson

import com.fasterxml.jackson.databind.module.SimpleModule
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId

class TypedIdModule : SimpleModule("TypedIdModule") {
  init {
    addSerializer(TypedIdSerializer())
    addDeserializer(TypedId::class.java, TypedIdDeserializer())
  }
}
