package io.craigmiller160.expensetrackerapi.testcontainers

import org.testcontainers.containers.PostgreSQLContainer

class PostgresContainer : PostgreSQLContainer<PostgresContainer>("postgres:12.5") {
  init {
    withDatabaseName("expense_tracker_test")
    withUsername("user")
    withPassword("password")
  }

  override fun start() {
    super.start()
    System.setProperty("spring.datasource.url", jdbcUrl)
    System.setProperty("spring.datasource.password", password)
    System.setProperty("spring.datasource.username", username)
  }
}
