package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.BaseIntegrationTest
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.web.types.CategorizeTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.DeleteTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.TransactionAndCategory
import io.craigmiller160.expensetrackerapi.web.types.TransactionResponse
import io.craigmiller160.expensetrackerapi.web.types.TransactionSearchRequest
import java.time.LocalDate
import javax.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put

class TransactionControllerTest : BaseIntegrationTest() {
  @Autowired private lateinit var transactionRepository: TransactionRepository
  @Autowired private lateinit var entityManager: EntityManager

  private lateinit var user1Categories: List<Category>
  private lateinit var user1Transactions: List<Transaction>
  private lateinit var user2Transactions: List<Transaction>

  @BeforeEach
  fun setup() {
    user1Categories = dataHelper.createDefaultCategories(1L)

    val (user1Txns, user2Txns) =
        (0..12)
            .map { index ->
              if (index % 2 == 0) {
                dataHelper.createTransaction(1L)
              } else {
                dataHelper.createTransaction(2L)
              }
            }
            .partition { it.userId == 1L }
    user1Transactions =
        user1Txns.mapIndexed { index, transaction ->
          if (index % 2 == 0) {
            transactionRepository.saveAndFlush(
                transaction.copy(categoryId = user1Categories[index % 3].id))
          } else {
            transaction
          }
        }
    user2Transactions = user2Txns
  }
  @Test
  fun `search - confirmed transactions only`() {
    val txn1 = transactionRepository.saveAndFlush(user1Transactions.first().copy(confirmed = true))
    val txn2 = transactionRepository.saveAndFlush(user1Transactions[1].copy(confirmed = true))
    transactionRepository.saveAndFlush(user2Transactions.first().copy(confirmed = true))
    val request = TransactionSearchRequest(confirmed = true, pageNumber = 0, pageSize = 100)

    val response =
        listOf(
            TransactionResponse.from(txn1), TransactionResponse.from(txn2, user1Categories.first()))

    mockMvc
        .get("/transactions") {
          secure = true
          header("Authorization", "Bearer $token")
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect {
          status { isOk() }
          content { json(objectMapper.writeValueAsString(response)) }
        }
  }

  @Test
  fun `search - unconfirmed transactions only`() {
    transactionRepository.saveAndFlush(user1Transactions.first().copy(confirmed = true))
    transactionRepository.saveAndFlush(user1Transactions[1].copy(confirmed = true))
    val request = TransactionSearchRequest(confirmed = false, pageNumber = 0, pageSize = 100)

    val response =
        listOf(
            TransactionResponse.from(user1Transactions[2]),
            TransactionResponse.from(user1Transactions[3], user1Categories[1]),
            TransactionResponse.from(user1Transactions[4]),
            TransactionResponse.from(user1Transactions[5], user1Categories[2]),
            TransactionResponse.from(user1Transactions[6]))

    mockMvc
        .get("/transactions") {
          secure = true
          header("Authorization", "Bearer $token")
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect {
          status { isOk() }
          content { json(objectMapper.writeValueAsString(response)) }
        }
  }

  @Test
  fun `search - start and end dates`() {
    val expected =
        user1Transactions
            .mapIndexed { index, txn ->
              transactionRepository.saveAndFlush(
                  txn.copy(expenseDate = LocalDate.now().minusDays(index.toLong())))
            }
            .filter { it.expenseDate.isAfter(LocalDate.now().minusDays(2)) }

    val request =
        TransactionSearchRequest(
            startDate = LocalDate.now().minusDays(2),
            endDate = LocalDate.now(),
            pageNumber = 0,
            pageSize = 100)

    val response = expected.map { TransactionResponse.from(it) }

    mockMvc
        .get("/transactions") {
          secure = true
          header("Authorization", "Bearer $token")
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect {
          status { isOk() }
          content { json(objectMapper.writeValueAsString(response)) }
        }
  }

  @Test
  fun deleteTransactions() {
    val request =
        DeleteTransactionsRequest(
            ids = listOf(user1Transactions.first().id, user2Transactions.first().id))

    mockMvc
        .delete("/transactions") {
          secure = true
          header("Authorization", "Bearer $token")
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect { status { isNoContent() } }

    entityManager.flush()

    assertThat(transactionRepository.findById(user1Transactions.first().id)).isEmpty
    assertThat(transactionRepository.findById(user2Transactions.first().id)).isPresent
  }

  @Test
  fun categorizeTransactions() {
    val uncategorizedTransaction = user1Transactions[5]
    assertThat(uncategorizedTransaction.categoryId).isNull()

    val categorizedTransaction = user1Transactions[6]
    assertThat(uncategorizedTransaction.categoryId)
        .isNotNull.isNotEqualTo(user1Categories.first().id)

    val request =
        CategorizeTransactionsRequest(
            transactionsAndCategories =
                listOf(
                    TransactionAndCategory(uncategorizedTransaction.id, user1Categories.first().id),
                    TransactionAndCategory(categorizedTransaction.id, user1Categories.first().id),
                    TransactionAndCategory(
                        user2Transactions.first().id, user1Categories.first().id)))

    mockMvc
        .put("/transactions/categorize") {
          secure = true
          header("Authorization", "Bearer $token")
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect { status { isNoContent() } }

    entityManager.flush()

    assertThat(transactionRepository.findById(uncategorizedTransaction.id))
        .isPresent
        .get()
        .hasFieldOrPropertyWithValue("categoryId", user1Categories.first().id)
    assertThat(transactionRepository.findById(categorizedTransaction.id))
        .isPresent
        .get()
        .hasFieldOrPropertyWithValue("categoryId", user1Categories.first().id)
    assertThat(transactionRepository.findById(user2Transactions.first().id))
        .isPresent
        .get()
        .hasFieldOrPropertyWithValue("categoryId", null)
  }
}
