package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import java.util.UUID
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@ExpenseTrackerIntegrationTest
class SecurityTest
@Autowired
constructor(private val mockMvc: MockMvc, private val authHelper: AuthenticationHelper) {
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
    val id = UUID.randomUUID().toString()
    val token = authHelper.createUser("norole_$id@gmail.com", listOf()).token
    mockMvc
      .get("/transaction-import/types") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isForbidden() } }
  }

  @Test
  fun `rejects no token`() {
    mockMvc
      .get("/transaction-import/types") { secure = true }
      .andExpect { status { isUnauthorized() } }
  }

  @Test
  fun `allows healthcheck without token`() {
    mockMvc.get("/actuator/health") { secure = true }.andExpect { status { isOk() } }
  }

  @Test
  fun `allows swagger without token`() {
    mockMvc.get("/v3/api-docs") { secure = true }.andExpect { status { isOk() } }
  }
}
