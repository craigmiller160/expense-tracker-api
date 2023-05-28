package io.craigmiller160.expensetrackerapi.common.data.typedid.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId

class TypedIdDeserializer : StdDeserializer<TypedId<*>>(TypedId::class.java) {
  private val delegate = UUIDDeserializer()
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TypedId<*> =
      TypedId<Any>(delegate.deserialize(p, ctxt))
}
