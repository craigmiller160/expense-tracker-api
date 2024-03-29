package io.craigmiller160.expensetrackerapi.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionViewRepository
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import io.craigmiller160.expensetrackerapi.testutils.DefaultUsers
import io.craigmiller160.expensetrackerapi.testutils.userTypedId
import io.craigmiller160.expensetrackerapi.web.types.transaction.CountAndOldest
import io.craigmiller160.expensetrackerapi.web.types.transaction.NeedsAttentionResponse
import jakarta.persistence.EntityManager
import java.math.BigDecimal
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
    private val entityManager: EntityManager,
    private val defaultUsers: DefaultUsers
) {
  private lateinit var token: String

  private lateinit var user1Categories: List<Category>
  private lateinit var user1CategoriesMap: Map<TypedId<CategoryId>, Category>
  private lateinit var user1Transactions: List<Transaction>
  private lateinit var user2Transactions: List<Transaction>

  @BeforeEach
  fun setup() {
    token = defaultUsers.primaryUser.token
    user1Categories = dataHelper.createDefaultCategories(defaultUsers.primaryUser.userTypedId)

    val (user1Txns, user2Txns) =
        (0..12)
            .map { index ->
              if (index % 2 == 0) {
                dataHelper.createTransaction(defaultUsers.primaryUser.userTypedId)
              } else {
                dataHelper.createTransaction(defaultUsers.secondaryUser.userTypedId)
              }
            }
            .partition { it.userId == defaultUsers.primaryUser.userTypedId }
    user1Transactions =
        user1Txns.mapIndexed { index, transaction ->
          if (index % 2 == 0) {
            transactionRepository.saveAndFlush(
                transaction.apply {
                  categoryId = user1Categories[index % 3].uid
                  confirmed = true
                })
          } else {
            transaction
          }
        }
    user2Transactions = user2Txns
    user1CategoriesMap = user1Categories.associateBy { it.uid }
  }

  @Test
  fun `get data on what records need attention, when all types need attention`() {
    val oldestUnconfirmed =
        transactionRepository.saveAndFlush(user1Transactions[0].apply { confirmed = false })
    val oldestDuplicate = transactionRepository.saveAndFlush(Transaction(user1Transactions[2]))
    val oldestPossibleRefund =
        transactionRepository.saveAndFlush(
            user1Transactions[3].apply { amount = user1Transactions[3].amount * BigDecimal("-1") })
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
          txn.apply {
            confirmed = true
            categoryId = user1Categories[0].uid
          })
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
