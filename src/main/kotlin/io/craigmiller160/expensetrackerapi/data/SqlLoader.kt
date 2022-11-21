package io.craigmiller160.expensetrackerapi.data

import java.util.concurrent.ConcurrentHashMap
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component

@Component
class SqlLoader(private val resourceLoader: ResourceLoader) {
  private val cache = ConcurrentHashMap<String, String>()
  fun loadSql(fileName: String): String =
    cache.computeIfAbsent(fileName) {
      resourceLoader.getResource("classpath:sql/$it").inputStream.reader().readText()
    }
}
