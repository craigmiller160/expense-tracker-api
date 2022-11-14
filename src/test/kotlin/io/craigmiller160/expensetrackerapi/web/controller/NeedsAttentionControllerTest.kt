package io.craigmiller160.expensetrackerapi.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionViewRepository
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testcore.OAuth2Extension
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import io.craigmiller160.expensetrackerapi.web.types.CountAndOldest
import io.craigmiller160.expensetrackerapi.web.types.NeedsAttentionResponse
import java.math.BigDecimal
import javax.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@ExpenseTrackerIntegrationTest
class NeedsAttentionControllerTest
@Autowired
constructor(
  private val transactionRepository: TransactionRepository,
  private val transactionViewRepository: TransactionViewRepository,
  private val dataHelper: DataHelper,
  private val mockMvc: MockMvc,
  private val objectMapper: ObjectMapper,
  private val entityManager: EntityManager
) {
  private lateinit var token: String

  private lateinit var user1Categories: List<Category>
  private lateinit var user1CategoriesMap: Map<TypedId<CategoryId>, Category>
  private lateinit var user1Transactions: List<Transaction>
  private lateinit var user2Transactions: List<Transaction>

  @BeforeEach
  fun setup() {
    token = OAuth2Extension.createJwt()
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
            transaction.copy(categoryId = user1Categories[index % 3].id, confirmed = true))
        } else {
          transaction
        }
      }
    user2Transactions = user2Txns
    user1CategoriesMap = user1Categories.associateBy { it.id }
  }

  @Test
  fun `get data on what records need attention, when all types need attention`() {
    val oldestUnconfirmed =
      transactionRepository.saveAndFlush(user1Transactions[0].copy(confirmed = false))
    val oldestDuplicate =
      transactionRepository.saveAndFlush(user1Transactions[2].copy(id = TypedId()))
    val oldestPossibleRefund =
      transactionRepository.saveAndFlush(
        user1Transactions[3].copy(amount = user1Transactions[3].amount * BigDecimal("-1")))
    val response =
      NeedsAttentionResponse(
        unconfirmed = CountAndOldest(count = 4, oldest = oldestUnconfirmed.expenseDate),
        uncategorized = CountAndOldest(count = 3, oldest = user1Transactions[1].expenseDate),
        duplicate = CountAndOldest(count = 2, oldest = oldestDuplicate.expenseDate),
        possibleRefund = CountAndOldest(count = 1, oldest = oldestPossibleRefund.expenseDate))
    mockMvc
      .get("/needs-attention") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(response), true) }
      }
  }

  @Test
  fun `get data on what records need attention, when no types need attention`() {
    user1Transactions.forEach { txn ->
      transactionRepository.saveAndFlush(
        txn.copy(confirmed = true, categoryId = user1Categories[0].id))
    }
    val response =
      NeedsAttentionResponse(
        unconfirmed = CountAndOldest(count = 0, oldest = null),
        uncategorized = CountAndOldest(count = 0, oldest = null),
        duplicate = CountAndOldest(count = 0, oldest = null),
        possibleRefund = CountAndOldest(count = 0, oldest = null))
    mockMvc
      .get("/needs-attention") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(response), true) }
      }
  }
}
