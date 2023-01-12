package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testutils.AuthenticationHelper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@ExpenseTrackerIntegrationTest
class SecurityTest
@Autowired
constructor(
  @Value("\${keycloak.auth-server-url}") private val authServerUrl: String,
  @Value("\${keycloak.resource}") private val clientId: String,
  @Value("\${keycloak.credentials.secret}") private val clientSecret: String,
  private val mockMvc: MockMvc,
  private val authHelper: AuthenticationHelper
) {
  @Test
  fun `allows valid token with access role`() {
    val token = authHelper.primaryUser.token
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
