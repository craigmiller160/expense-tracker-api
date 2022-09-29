package io.craigmiller160.expensetrackerapi.web.controller

import arrow.core.Either
import arrow.core.flatMap
import io.craigmiller160.expensetrackerapi.BaseIntegrationTest
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.service.TransactionImportType
import io.craigmiller160.expensetrackerapi.testutils.ResourceUtils
import io.craigmiller160.expensetrackerapi.web.types.ImportTypeResponse
import io.kotest.assertions.arrow.core.shouldBeRight
import java.math.BigDecimal
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart

class TransactionImportControllerTest : BaseIntegrationTest() {
  @Autowired private lateinit var transactionRepository: TransactionRepository
  // TODO need all expenses to be negative
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

    val transactions = transactionRepository.findAllByOrderByExpenseDateAscDescriptionAsc()
    assertThat(transactions).hasSize(57)

    assertThat(transactions.first())
      .hasFieldOrPropertyWithValue("userId", 1L)
      .hasFieldOrPropertyWithValue("expenseDate", LocalDate.of(2022, 4, 18))
      .hasFieldOrPropertyWithValue("description", "WAWA 5127 TAMPA FL")
      .hasFieldOrPropertyWithValue("amount", BigDecimal("44.72"))

    assertThat(transactions[transactions.size - 14])
      .hasFieldOrPropertyWithValue("userId", 1L)
      .hasFieldOrPropertyWithValue("expenseDate", LocalDate.of(2022, 9, 5))
      .hasFieldOrPropertyWithValue(
        "description", "DIRECTPAY FULL BALANCESEE DETAILS OF YOUR NEXT DIRECTPAY BELOW")
      .hasFieldOrPropertyWithValue("amount", BigDecimal("-1928.54"))

    assertThat(transactions.last())
      .hasFieldOrPropertyWithValue("userId", 1L)
      .hasFieldOrPropertyWithValue("expenseDate", LocalDate.of(2022, 5, 18))
      .hasFieldOrPropertyWithValue("description", "PANDA EXPRESS 1679 RIVERVIEW FL")
      .hasFieldOrPropertyWithValue("amount", BigDecimal("5.81"))
  }

  @Test
  fun `importTransactions - CHASE_CSV`() {
    ResourceUtils.getResourceBytes("data/chase1.csv")
      .flatMap { bytes ->
        Either.catch {
          mockMvc
            .multipart("/transaction-import?type=${TransactionImportType.CHASE_CSV.name}") {
              secure = true
              header("Authorization", "Bearer $token")
              header("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE)
              file("file", bytes)
            }
            .andExpect {
              status { isOk() }
              content { json("""{"transactionsImported":23}""") }
            }
        }
      }
      .shouldBeRight()

    val transactions = transactionRepository.findAllByOrderByExpenseDateAscDescriptionAsc()
    assertThat(transactions).hasSize(23)

    assertThat(transactions.first())
      .hasFieldOrPropertyWithValue("userId", 1L)
      .hasFieldOrPropertyWithValue("expenseDate", LocalDate.of(2022, 5, 23))
      .hasFieldOrPropertyWithValue(
        "description", "FID BKG SVC LLC  MONEYLINE                  PPD ID: 1035141383")
      .hasFieldOrPropertyWithValue("amount", BigDecimal("-250.00"))

    assertThat(transactions[20])
      .hasFieldOrPropertyWithValue("userId", 1L)
      .hasFieldOrPropertyWithValue("expenseDate", LocalDate.of(2022, 6, 15))
      .hasFieldOrPropertyWithValue(
        "description", "C89303 CLEARSPEN DIR DEP                    PPD ID: 4462283648")
      .hasFieldOrPropertyWithValue("amount", BigDecimal("4097.76"))

    assertThat(transactions[21])
      .hasFieldOrPropertyWithValue("userId", 1L)
      .hasFieldOrPropertyWithValue("expenseDate", LocalDate.of(2022, 6, 15))
      .hasFieldOrPropertyWithValue(
        "description", "FRONTIER COMM CORP WE 800-921-8101 CT        06/14")
      .hasFieldOrPropertyWithValue("amount", BigDecimal("-64.99"))
  }
}
