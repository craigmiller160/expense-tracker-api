package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.BaseIntegrationTest
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.web.types.CategorizeTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.TransactionAndCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.put

class TransactionControllerTest : BaseIntegrationTest() {
  @Autowired private lateinit var transactionRepository: TransactionRepository

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
    TODO()
  }

  @Test
  fun `search - unconfirmed transactions only`() {
    TODO()
  }

  @Test
  fun `search - start and end dates`() {
    TODO()
  }

  @Test
  fun deleteTransactions() {
    TODO()
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
        .andExpect { status { isOk() } }

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
