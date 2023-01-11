package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import java.util.Base64
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@ExpenseTrackerIntegrationTest
class SecurityTest
@Autowired
constructor(
  @Value("\${keycloak.auth-server-url}") private val authServerUrl: String,
  @Value("\${keycloak.resource}") private val clientId: String,
  @Value("\${keycloak.credentials.secret}") private val clientSecret: String,
  private val mockMvc: MockMvc
) {
  private val restTemplate: RestTemplate = RestTemplate()

  private fun login(): String {
    val formData =
      LinkedMultiValueMap(
        mapOf(
          "grant_type" to listOf("password"),
          "client_id" to listOf(clientId),
          "username" to listOf("craig@gmail.com"),
          "password" to listOf("s3cr3t")))
    val basicAuth =
      "Basic ${Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())}"
    val headers = HttpHeaders()
    headers.add("Authorization", basicAuth)
    val entity = HttpEntity(formData, headers)
    return restTemplate
      .postForEntity(
        "$authServerUrl/realms/apps-dev/protocol/openid-connect/token", entity, Map::class.java)
      .body
      ?.get("access_token")!!
      as String
  }
  @Test
  fun `allows valid token with access role`() {
    val token = login()
    mockMvc
      .get("/transaction-import/types") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isOk() } }
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
