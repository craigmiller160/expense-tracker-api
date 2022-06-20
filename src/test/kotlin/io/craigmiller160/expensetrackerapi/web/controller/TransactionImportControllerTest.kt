package io.craigmiller160.expensetrackerapi.web.controller

import arrow.core.Either
import arrow.core.flatMap
import io.craigmiller160.expensetrackerapi.BaseIntegrationTest
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.service.TransactionImportType
import io.craigmiller160.expensetrackerapi.testutils.ResourceUtils
import io.craigmiller160.expensetrackerapi.web.types.ImportTypeResponse
import io.kotest.assertions.arrow.core.shouldBeRight
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart

class TransactionImportControllerTest : BaseIntegrationTest() {
  @Autowired private lateinit var transactionRepository: TransactionRepository
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
    ResourceUtils.getResourceBytes("data/discover1.csv")
        .flatMap { bytes ->
          Either.catch {
            mockMvc
                .multipart("/transaction-import?type=${TransactionImportType.DISCOVER_CSV.name}") {
                  secure = true
                  header("Authorization", "Bearer $token")
                  header("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE)
                  file("file", bytes)
                }
                .andExpect {
                  status { isOk() }
                  content { json("""{"transactionsImported":57}""") }
                }
          }
        }
        .shouldBeRight()

    val transactions = transactionRepository.findAllByOrderByExpenseDateAsc()
    assertThat(transactions).hasSize(57)
  }
}
