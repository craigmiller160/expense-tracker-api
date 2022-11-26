package io.craigmiller160.expensetrackerapi.data

import com.github.mustachejava.DefaultMustacheFactory
import io.craigmiller160.expensetrackerapi.data.mustache.MustacheSqlTemplate
import java.util.concurrent.ConcurrentHashMap
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component

@Component
class SqlLoader(private val resourceLoader: ResourceLoader) {
  private val rawSqlCache = ConcurrentHashMap<String, String>()
  private val sqlMustacheCache = ConcurrentHashMap<String, MustacheSqlTemplate>()
  private val mustacheFactory = DefaultMustacheFactory()
  fun loadSql(fileName: String): String =
    rawSqlCache.computeIfAbsent(fileName) {
      resourceLoader.getResource("classpath:sql/$it").inputStream.reader().readText()
    }

  fun loadSqlMustacheTemplate(fileName: String): MustacheSqlTemplate =
    sqlMustacheCache.computeIfAbsent(fileName) {
      loadSql(fileName).let(mustacheFactory::compile).let { MustacheSqlTemplate(it) }
    }
}
