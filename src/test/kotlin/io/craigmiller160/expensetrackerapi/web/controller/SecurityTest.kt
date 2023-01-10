package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@ExpenseTrackerIntegrationTest
class SecurityTest {
  private val restTemplate: RestTemplate = RestTemplate()

  private fun login(): String {
    val formData =
      LinkedMultiValueMap(
        mapOf(
          "grant_type" to listOf("password"),
          "client_id" to listOf("expense-tracker-api"),
          "username" to listOf("craig"),
          "password" to listOf("password")))
    restTemplate.postForEntity("", formData, String::class.java)
  }
  @Test
  fun `allows valid token with access role`() {
    TODO()
  }

  @Test
  fun `rejects valid token without access role`() {
    TODO()
  }

  @Test
  fun `allows healthcheck without token`() {
    TODO()
  }

  @Test
  fun `allows swagger without token`() {
    TODO()
  }
}
