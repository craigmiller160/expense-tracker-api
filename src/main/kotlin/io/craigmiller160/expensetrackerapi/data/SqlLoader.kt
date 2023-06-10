package io.craigmiller160.expensetrackerapi.data

import java.io.Reader
import java.util.concurrent.ConcurrentHashMap
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component

@Component
class SqlLoader(private val resourceLoader: ResourceLoader) {
  private val rawSqlCache = ConcurrentHashMap<String, String>()

  private fun openSqlReader(fileName: String): Reader =
      resourceLoader.getResource("classpath:sql/$fileName").inputStream.reader()
  fun loadSql(fileName: String): String =
      rawSqlCache.computeIfAbsent(fileName) { openSqlReader(fileName).readText() }
}
