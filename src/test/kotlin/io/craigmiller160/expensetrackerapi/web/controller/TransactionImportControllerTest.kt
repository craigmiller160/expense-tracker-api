package io.craigmiller160.expensetrackerapi.web.controller

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrHandle
import io.craigmiller160.expensetrackerapi.BaseIntegrationTest
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.TransactionDuplicateRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.data.utils.TransactionContentHash
import io.craigmiller160.expensetrackerapi.service.TransactionImportType
import io.craigmiller160.expensetrackerapi.testutils.ResourceUtils
import io.craigmiller160.expensetrackerapi.web.types.ImportTypeResponse
import io.kotest.assertions.arrow.core.shouldBeRight
import java.math.BigDecimal
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart

class TransactionImportControllerTest : BaseIntegrationTest() {
  @Autowired private lateinit var transactionRepository: TransactionRepository
  @Autowired private lateinit var transactionDuplicateRepository: TransactionDuplicateRepository
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
          contentHash = TransactionContentHash.hash(expenseDate, amount, description)))
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
        content { json("""{"transactionsImported":57}""") }
      }

    val allDuplicateTransactions =
      transactionRepository.findAllByUserIdAndContentHashOrderByCreated(1L, transaction.contentHash)
    val lastTransaction = allDuplicateTransactions.last()
    val nextToLastTransaction = allDuplicateTransactions[allDuplicateTransactions.size - 2]
    assertArrayEquals(lastTransaction.contentHash, nextToLastTransaction.contentHash)

    val duplicates =
      transactionDuplicateRepository.findAllByUserIdAndNewTransactionId(1L, lastTransaction.id)
    assertThat(duplicates).hasSize(1)
    assertThat(duplicates.first())
      .hasFieldOrPropertyWithValue("newTransactionId", lastTransaction.id)
      .hasFieldOrPropertyWithValue("possibleDuplicateTransactionId", nextToLastTransaction.id)
  }

  @Test
  fun `importTransactions - DISCOVER_CSV with duplicates in import`() {
    val duplicateLine =
      """05/18/2022,05/18/2022,"PANDA EXPRESS 1679 RIVERVIEW FL",5.81,"Restaurants""""
    val csvBytes =
      ResourceUtils.getResourceBytes("data/discover1.csv")
        .map { "${String(it)}\n$duplicateLine".toByteArray() }
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
        content { json("""{"transactionsImported":57}""") }
      }

    val contentHash =
      TransactionContentHash.hash(
        LocalDate.of(2022, 5, 18), BigDecimal("-5.81"), "PANDA EXPRESS 1679 RIVERVIEW FL")

    val allDuplicateTransactions =
      transactionRepository.findAllByUserIdAndContentHashOrderByCreated(1L, contentHash)
    val lastTransaction = allDuplicateTransactions.last()
    val nextToLastTransaction = allDuplicateTransactions[allDuplicateTransactions.size - 2]
    assertArrayEquals(lastTransaction.contentHash, nextToLastTransaction.contentHash)

    val duplicates =
      transactionDuplicateRepository.findAllByUserIdAndNewTransactionId(1L, lastTransaction.id)
    assertThat(duplicates).hasSize(1)
    assertThat(duplicates.first())
      .hasFieldOrPropertyWithValue("newTransactionId", lastTransaction.id)
      .hasFieldOrPropertyWithValue("possibleDuplicateTransactionId", nextToLastTransaction.id)
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
      .hasFieldOrPropertyWithValue("description", "PARTY CITY 1084 TAMPA FL01837R")
      .hasFieldOrPropertyWithValue("amount", BigDecimal("-36.87"))

    assertThat(transactions[41])
      .hasFieldOrPropertyWithValue("userId", 1L)
      .hasFieldOrPropertyWithValue("expenseDate", LocalDate.of(2022, 5, 9))
      .hasFieldOrPropertyWithValue(
        "description", "DIRECTPAY FULL BALANCESEE DETAILS OF YOUR NEXT DIRECTPAY BELOW")
      .hasFieldOrPropertyWithValue("amount", BigDecimal("1928.54"))

    assertThat(transactions.last())
      .hasFieldOrPropertyWithValue("userId", 1L)
      .hasFieldOrPropertyWithValue("expenseDate", LocalDate.of(2022, 5, 18))
      .hasFieldOrPropertyWithValue("description", "PANDA EXPRESS 1679 RIVERVIEW FL")
      .hasFieldOrPropertyWithValue("amount", BigDecimal("-5.81"))
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
