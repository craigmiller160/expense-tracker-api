package io.craigmiller160.expensetrackerapi.data

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import java.util.concurrent.ConcurrentHashMap
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component

@Component
class SqlLoader(private val resourceLoader: ResourceLoader) {
  private val rawSqlCache = ConcurrentHashMap<String, String>()
  private val sqlMustacheCache = ConcurrentHashMap<String, Mustache>()
  private val mustacheFactory = DefaultMustacheFactory()
  fun loadSql(fileName: String): String =
    rawSqlCache.computeIfAbsent(fileName) {
      resourceLoader.getResource("classpath:sql/$it").inputStream.reader().readText()
    }

  fun loadSqlMustacheTemplate(fileName: String): Mustache =
    sqlMustacheCache.computeIfAbsent(fileName) { loadSql(fileName).let(mustacheFactory::compile) }
}
