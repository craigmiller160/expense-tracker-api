package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.BaseIntegrationTest
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TransactionControllerTest : BaseIntegrationTest() {
  @Autowired private lateinit var transactionRepository: TransactionRepository

  private lateinit var user1Categories: List<Category>
  private lateinit var user1Transactions: List<Transaction>

  @BeforeEach
  fun setup() {
    user1Categories = dataHelper.createDefaultCategories(1L)

    user1Transactions =
        (0..12)
            .map { index ->
              if (index % 2 == 0) {
                dataHelper.createTransaction(1L)
              } else {
                dataHelper.createTransaction(2L)
              }
            }
            .partition { it.userId == 1L }
            .first.mapIndexed { index, transaction ->
              transactionRepository.saveAndFlush(
                  transaction.copy(categoryId = user1Categories[index % 3].id))
            }
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
    TODO()
  }
}
