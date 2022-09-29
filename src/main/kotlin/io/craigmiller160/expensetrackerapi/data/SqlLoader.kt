package io.craigmiller160.expensetrackerapi.data

import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component

@Component
class SqlLoader(private val resourceLoader: ResourceLoader) {
  fun loadSql(fileName: String): String =
    resourceLoader.getResource("sql/$fileName").inputStream.reader().readText()
}
