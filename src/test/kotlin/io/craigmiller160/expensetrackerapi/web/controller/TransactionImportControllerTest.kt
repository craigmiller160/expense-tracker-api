package io.craigmiller160.expensetrackerapi.web.controller

import arrow.core.getOrHandle
import io.craigmiller160.expensetrackerapi.BaseIntegrationTest
import io.craigmiller160.expensetrackerapi.function.tryEither
import io.craigmiller160.expensetrackerapi.service.TransactionImportType
import io.craigmiller160.expensetrackerapi.testutils.ResourceUtils
import io.craigmiller160.expensetrackerapi.web.types.ImportTypeResponse
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart

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
    tryEither
        .eager {
          val bytes = ResourceUtils.getResourceBytes("data/discover1.csv").bind()

          mockMvc
              .multipart("/transaction-import?type=${TransactionImportType.DISCOVER_CSV.name}") {
                secure = true
                header("Authorization", "Bearer $token")
                file("content.txt", bytes)
              }
              .andExpect { status { isOk() } }
          // TODO test content
        }
        .getOrHandle { throw it }
  }
}
