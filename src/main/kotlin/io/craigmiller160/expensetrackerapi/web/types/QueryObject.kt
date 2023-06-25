package io.craigmiller160.expensetrackerapi.web.types

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

interface QueryObject {
  fun toQueryString(): String =
      fieldsToQueryParams()
          .filter { it.second != null }
          .map { (first, second) -> first to URLEncoder.encode(second, StandardCharsets.UTF_8) }
          .joinToString("&") { "${it.first}=${it.second}" }

  fun fieldsToQueryParams(): List<Pair<String, String?>>
}
