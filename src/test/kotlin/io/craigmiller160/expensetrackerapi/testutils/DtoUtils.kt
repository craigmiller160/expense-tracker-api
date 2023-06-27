package io.craigmiller160.expensetrackerapi.testutils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.craigmiller160.expensetrackerapi.common.utils.DateUtils
import io.craigmiller160.expensetrackerapi.web.types.QueryObject
import java.lang.IllegalArgumentException
import java.time.LocalDate

private val mapTypeRef = jacksonTypeRef<Map<String, Any?>>()

fun QueryObject.toQueryString(objectMapper: ObjectMapper): String {
  val map: Map<String, Any?> =
      objectMapper.writeValueAsString(this).let { objectMapper.readValue(it, mapTypeRef) }
  return map.entries
      .filter { (_, value) -> value != null }
      .map { (key, value) -> key to convertQueryValue(value!!) }
      .joinToString("&") { "${it.first}=${it.second}" }
}

private fun convertQueryValue(value: Any): String =
    when (value) {
      is Int,
      is Long,
      is Float,
      is Double,
      is Boolean -> value.toString()
      is String -> value
      is Collection<*> -> value.joinToString(",") { it.toString() }
      is Enum<*> -> value.name
      is LocalDate -> DateUtils.format(value)
      else ->
          throw IllegalArgumentException(
              "Invalid value to convert for query string: ${value.javaClass.name}")
    }
