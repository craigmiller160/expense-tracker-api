package io.craigmiller160.expensetrackerapi.testcontainers

import org.testcontainers.containers.PostgreSQLContainer

class PostgresContainer : PostgreSQLContainer<PostgresContainer>("postgres:14.5") {
  companion object {
    val INSTANCE = PostgresContainer()
  }
  init {
    withDatabaseName("expense_tracker_test")
    withUsername("user")
    withPassword("password")
    withReuse(true)
  }

  override fun start() {
    super.start()
    System.setProperty("spring.datasource.url", jdbcUrl)
    System.setProperty("spring.datasource.password", password)
    System.setProperty("spring.datasource.username", username)
  }
}
