package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.utils.DateUtils
import java.lang.IllegalArgumentException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate

interface QueryObject {
  fun toQueryString(): String =
      fieldsToQueryParams()
          .filter { it.second != null }
          .map { (first, second) -> first to convertValue(second!!) }
          .joinToString("&") { "${it.first}=${it.second}" }

  fun fieldsToQueryParams(): List<Pair<String, Any?>>
}

private fun convertValue(value: Any): String =
    when (value) {
      is String -> value
      is List<*> ->
          value.joinToString(",") { URLEncoder.encode(it.toString(), StandardCharsets.UTF_8) }
      is Enum<*> -> value.name
      is LocalDate -> DateUtils.format(value)
      else ->
          throw IllegalArgumentException(
              "Invalid value to convert for query string: ${value.javaClass.name}")
    }
