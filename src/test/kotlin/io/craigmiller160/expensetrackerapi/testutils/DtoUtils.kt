package io.craigmiller160.expensetrackerapi.testutils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.craigmiller160.expensetrackerapi.common.utils.DateUtils
import io.craigmiller160.expensetrackerapi.web.types.QueryObject
import java.lang.IllegalArgumentException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate

private val objectMapper = jacksonObjectMapper()
private val mapTypeRef = jacksonTypeRef<Map<String, Any?>>()

fun QueryObject.toQueryString(): String {
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
      is Boolean -> URLEncoder.encode(value.toString(), StandardCharsets.UTF_8)
      is String -> URLEncoder.encode(value, StandardCharsets.UTF_8)
      is Collection<*> ->
          value.joinToString(",") { URLEncoder.encode(it.toString(), StandardCharsets.UTF_8) }
      is Enum<*> -> URLEncoder.encode(value.name, StandardCharsets.UTF_8)
      is LocalDate -> URLEncoder.encode(DateUtils.format(value), StandardCharsets.UTF_8)
      else ->
          throw IllegalArgumentException(
              "Invalid value to convert for query string: ${value.javaClass.name}")
    }
