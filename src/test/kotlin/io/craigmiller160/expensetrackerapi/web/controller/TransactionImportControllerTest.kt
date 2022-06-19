package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.BaseIntegrationTest
import io.craigmiller160.expensetrackerapi.service.TransactionImportType
import io.craigmiller160.expensetrackerapi.web.types.ImportTypeResponse
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get

class TransactionImportControllerTest : BaseIntegrationTest() {
  @Test
  fun getImportTypes() {
    val expectedResponse =
        TransactionImportType.values().map { ImportTypeResponse(it.name, it.displayName) }
    mockMvc
        .get("/transaction-import/types") {
          secure = true
          header("Authorization", "Bearer $token")
        }
        .andExpect {
          status { isOk() }
          content { json(objectMapper.writeValueAsString(expectedResponse)) }
        }
  }

  @Test
  fun `importTransactions - DISCOVER_CSV`() {
    TODO()
  }
}
