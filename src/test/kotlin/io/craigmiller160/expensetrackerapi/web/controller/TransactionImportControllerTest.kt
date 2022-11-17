package io.craigmiller160.expensetrackerapi.web.controller

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrHandle
import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.data.utils.TransactionContentHash
import io.craigmiller160.expensetrackerapi.service.TransactionImportType
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testcore.OAuth2Extension
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import io.craigmiller160.expensetrackerapi.testutils.ResourceUtils
import io.craigmiller160.expensetrackerapi.web.types.importing.ImportTypeResponse
import io.kotest.assertions.arrow.core.shouldBeRight
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart

@ExpenseTrackerIntegrationTest
class TransactionImportControllerTest
@Autowired
constructor(
  private val transactionRepository: TransactionRepository,
  private val mockMvc: MockMvc,
  private val objectMapper: ObjectMapper,
  private val entityManager: EntityManager,
  private val dataHelper: DataHelper
) {
  private lateinit var token: String

  @BeforeEach
  fun setup() {
    token = OAuth2Extension.createJwt()
  }
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
        content { json(objectMapper.writeValueAsString(expectedResponse), true) }
      }
  }

  @Test
  fun `importTransactions - DISCOVER_CSV with duplicates already in database`() {
    val expenseDate = LocalDate.of(2022, 5, 18)
    val description = "PANDA EXPRESS 1679 RIVERVIEW FL"
    val amount = BigDecimal("-5.81")
    val transaction =
      transactionRepository.save(
        Transaction(
          userId = 1L,
          expenseDate = expenseDate,
          description = description,
          amount = amount,
          confirmed = false,
          contentHash = TransactionContentHash.hash(1L, expenseDate, amount, description)))
    val csvBytes = ResourceUtils.getResourceBytes("data/discover1.csv").getOrHandle { throw it }

    mockMvc
      .multipart("/transaction-import?type=${TransactionImportType.DISCOVER_CSV.name}") {
        secure = true
        header("Authorization", "Bearer $token")
        header("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE)
        file("file", csvBytes)
      }
      .andExpect {
        status { isOk() }
        content { json("""{"transactionsImported":57}""", true) }
      }

    entityManager.flush()
    entityManager.clear()

    val allDuplicateTransactions =
      transactionRepository.findAllByUserIdAndContentHashInOrderByCreated(
        1L, listOf(transaction.contentHash))
    val lastTransaction = allDuplicateTransactions.last()
    val nextToLastTransaction = allDuplicateTransactions[allDuplicateTransactions.size - 2]
    assertEquals(lastTransaction.contentHash, nextToLastTransaction.contentHash)
  }

  @Test
  fun `importTransactions - DISCOVER_CSV with duplicates in import`() {
    val duplicateLine =
      """05/18/2022,05/18/2022,"PANDA EXPRESS 1679 RIVERVIEW FL",5.81,"Restaurants""""
    val csvBytes =
      ResourceUtils.getResourceBytes("data/discover1.csv")
        .map { "${String(it).trim()}\n$duplicateLine".toByteArray() }
        .getOrHandle { throw it }

    mockMvc
      .multipart("/transaction-import?type=${TransactionImportType.DISCOVER_CSV.name}") {
        secure = true
        header("Authorization", "Bearer $token")
        header("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE)
        file("file", csvBytes)
      }
      .andExpect {
        status { isOk() }
        content { json("""{"transactionsImported":58}""", true) }
      }

    entityManager.flush()
    entityManager.clear()

    val contentHash =
      TransactionContentHash.hash(
        1L, LocalDate.of(2022, 5, 18), BigDecimal("-5.81"), "PANDA EXPRESS 1679 RIVERVIEW FL")

    val allDuplicateTransactions =
      transactionRepository.findAllByUserIdAndContentHashInOrderByCreated(1L, listOf(contentHash))
    val lastTransaction = allDuplicateTransactions.last()
    val nextToLastTransaction = allDuplicateTransactions[allDuplicateTransactions.size - 2]
    assertEquals(lastTransaction.contentHash, nextToLastTransaction.contentHash)
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
              content { json("""{"transactionsImported":57}""", true) }
            }
        }
      }
      .shouldBeRight()

    entityManager.flush()
    entityManager.clear()

    val transactions = transactionRepository.findAllByUserIdOrderByExpenseDateAscDescriptionAsc(1L)
    assertThat(transactions).hasSize(57)

    assertThat(transactions.first())
      .hasFieldOrPropertyWithValue("userId", 1L)
      .hasFieldOrPropertyWithValue("expenseDate", LocalDate.of(2022, 4, 18))
      .hasFieldOrPropertyWithValue("description", "PARTY CITY 1084 TAMPA FL01837R")
      .hasFieldOrPropertyWithValue("amount", BigDecimal("-36.87"))
      .hasFieldOrPropertyWithValue("categoryId", null)

    assertThat(transactions[41])
      .hasFieldOrPropertyWithValue("userId", 1L)
      .hasFieldOrPropertyWithValue("expenseDate", LocalDate.of(2022, 5, 9))
      .hasFieldOrPropertyWithValue(
        "description", "DIRECTPAY FULL BALANCESEE DETAILS OF YOUR NEXT DIRECTPAY BELOW")
      .hasFieldOrPropertyWithValue("amount", BigDecimal("1928.54"))
      .hasFieldOrPropertyWithValue("categoryId", null)

    assertThat(transactions.last())
      .hasFieldOrPropertyWithValue("userId", 1L)
      .hasFieldOrPropertyWithValue("expenseDate", LocalDate.of(2022, 5, 18))
      .hasFieldOrPropertyWithValue("description", "PANDA EXPRESS 1679 RIVERVIEW FL")
      .hasFieldOrPropertyWithValue("amount", BigDecimal("-5.81"))
      .hasFieldOrPropertyWithValue("categoryId", null)
  }

  @Test
  fun `importTransactions - DISCOVER_CSV, with auto-categorization rules`() {
    TODO()
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
              content { json("""{"transactionsImported":23}""", true) }
            }
        }
      }
      .shouldBeRight()

    entityManager.flush()
    entityManager.clear()

    val transactions = transactionRepository.findAllByUserIdOrderByExpenseDateAscDescriptionAsc(1L)
    assertThat(transactions).hasSize(23)

    assertThat(transactions.first())
      .hasFieldOrPropertyWithValue("userId", 1L)
      .hasFieldOrPropertyWithValue("expenseDate", LocalDate.of(2022, 5, 23))
      .hasFieldOrPropertyWithValue(
        "description", "FID BKG SVC LLC  MONEYLINE                  PPD ID: 1035141383")
      .hasFieldOrPropertyWithValue("amount", BigDecimal("-250.00"))
      .hasFieldOrPropertyWithValue("categoryId", null)

    assertThat(transactions[20])
      .hasFieldOrPropertyWithValue("userId", 1L)
      .hasFieldOrPropertyWithValue("expenseDate", LocalDate.of(2022, 6, 15))
      .hasFieldOrPropertyWithValue(
        "description", "C89303 CLEARSPEN DIR DEP                    PPD ID: 4462283648")
      .hasFieldOrPropertyWithValue("amount", BigDecimal("4097.76"))
      .hasFieldOrPropertyWithValue("categoryId", null)

    assertThat(transactions[21])
      .hasFieldOrPropertyWithValue("userId", 1L)
      .hasFieldOrPropertyWithValue("expenseDate", LocalDate.of(2022, 6, 15))
      .hasFieldOrPropertyWithValue(
        "description", "FRONTIER COMM CORP WE 800-921-8101 CT        06/14")
      .hasFieldOrPropertyWithValue("amount", BigDecimal("-64.99"))
      .hasFieldOrPropertyWithValue("categoryId", null)
  }
}
