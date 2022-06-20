package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.BaseIntegrationTest
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TransactionControllerTest : BaseIntegrationTest() {
  private lateinit var user1Categories: List<Category>
  private lateinit var user1Transactions: List<Transaction>

  @BeforeEach
  fun setup() {
    user1Categories = dataHelper.createDefaultCategories(1L)

    user1Transactions =
        (0..12)
            .map { index ->
              if (index % 2 == 0) {
                dataHelper.createTransaction(1L, user1Categories[index % 3].id)
              } else {
                dataHelper.createTransaction(2L)
              }
            }
            .partition { it.userId == 1L }
            .first
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
