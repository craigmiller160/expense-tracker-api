package io.craigmiller160.expensetrackerapi.common.data.typedid.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.databind.ser.std.UUIDSerializer
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId

class TypedIdSerializer : StdSerializer<TypedId<*>>(TypedId::class.java, false) {
  private val delegate = UUIDSerializer()
  override fun serialize(value: TypedId<*>, gen: JsonGenerator, provider: SerializerProvider) {
    delegate.serialize(value.uuid, gen, provider)
  }
}
