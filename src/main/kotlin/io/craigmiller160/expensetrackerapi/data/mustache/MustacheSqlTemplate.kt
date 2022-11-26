package io.craigmiller160.expensetrackerapi.data.mustache

import com.github.mustachejava.Mustache
import java.io.StringWriter

class MustacheSqlTemplate(private val mustache: Mustache) {
  fun executeWithParams(vararg params: String): String {
    val map = params.associateWith { true }
    val writer = StringWriter()
    mustache.execute(writer, map)
    return writer.toString()
  }
}
