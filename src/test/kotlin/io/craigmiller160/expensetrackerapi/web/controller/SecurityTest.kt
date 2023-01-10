package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@ExpenseTrackerIntegrationTest
class SecurityTest(@Value("\${keycloak.auth-server-url}") private val authServerUrl: String) {
  private val restTemplate: RestTemplate = RestTemplate()

  private fun login(): String {
    val formData =
      LinkedMultiValueMap(
        mapOf(
          "grant_type" to listOf("password"),
          "client_id" to listOf("expense-tracker-api"),
          "username" to listOf("craig@gmail.com"),
          "password" to listOf("s3cr3t")))
    return restTemplate
      .postForEntity(
        "$authServerUrl/realms/apps-dev/protocol/openid-connect/token",
        formData,
        String::class.java)
      .body!!
  }
  @Test
  fun `allows valid token with access role`() {
    val response = login()
    println(response)
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
