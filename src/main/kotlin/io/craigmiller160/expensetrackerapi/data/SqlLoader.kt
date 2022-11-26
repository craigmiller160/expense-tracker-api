package io.craigmiller160.expensetrackerapi.data

import com.github.mustachejava.DefaultMustacheFactory
import io.craigmiller160.expensetrackerapi.data.mustache.MustacheSqlTemplate
import java.io.Reader
import java.util.concurrent.ConcurrentHashMap
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component

@Component
class SqlLoader(private val resourceLoader: ResourceLoader) {
  private val rawSqlCache = ConcurrentHashMap<String, String>()
  private val sqlMustacheCache = ConcurrentHashMap<String, MustacheSqlTemplate>()
  private val mustacheFactory = DefaultMustacheFactory()

  private fun openSqlReader(fileName: String): Reader =
    resourceLoader.getResource("classpath:sql/$fileName").inputStream.reader()
  fun loadSql(fileName: String): String =
    rawSqlCache.computeIfAbsent(fileName) { openSqlReader(fileName).readText() }

  fun loadSqlMustacheTemplate(fileName: String): MustacheSqlTemplate =
    sqlMustacheCache.computeIfAbsent(fileName) {
      mustacheFactory.compile(openSqlReader(fileName), fileName).let { MustacheSqlTemplate(it) }
    }
}
