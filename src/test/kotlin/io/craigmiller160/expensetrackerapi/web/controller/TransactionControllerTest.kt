package io.craigmiller160.expensetrackerapi.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.model.TransactionCommon
import io.craigmiller160.expensetrackerapi.data.repository.LastRuleAppliedRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionViewRepository
import io.craigmiller160.expensetrackerapi.extension.flushAndClear
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testcore.OAuth2Extension
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import io.craigmiller160.expensetrackerapi.web.types.*
import io.craigmiller160.expensetrackerapi.web.types.transaction.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Comparator
import java.util.UUID
import javax.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@ExpenseTrackerIntegrationTest
class TransactionControllerTest
@Autowired
constructor(
  private val transactionRepository: TransactionRepository,
  private val transactionViewRepository: TransactionViewRepository,
  private val lastRuleAppliedRepository: LastRuleAppliedRepository,
  private val dataHelper: DataHelper,
  private val mockMvc: MockMvc,
  private val objectMapper: ObjectMapper,
  private val entityManager: EntityManager
) {
  private val transactionComparator: Comparator<TransactionCommon> = Comparator { txn1, txn2 ->
    val expenseDateCompare = txn1.expenseDate.compareTo(txn2.expenseDate)
    if (expenseDateCompare == 0) {
      txn1.description.compareTo(txn2.description)
    } else {
      expenseDateCompare
    }
  }

  private lateinit var token: String

  private lateinit var user1Categories: List<Category>
  private lateinit var user1CategoriesMap: Map<TypedId<CategoryId>, Category>
  private lateinit var user1Transactions: List<Transaction>
  private lateinit var user2Transactions: List<Transaction>
  private lateinit var rule: AutoCategorizeRule

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
            transaction.apply {
              categoryId = user1Categories[index % 3].recordId
              confirmed = true
            })
        } else {
          transaction
        }
      }
    user2Transactions = user2Txns
    user1CategoriesMap = user1Categories.associateBy { it.recordId }
    rule = dataHelper.createRule(1L, user1Categories[0].recordId)
  }

  @Test
  fun `search - with no categories, sort by EXPENSE_DATE DESC`() {
    val request =
      SearchTransactionsRequest(
        isCategorized = false,
        pageNumber = 0,
        pageSize = 100,
        sortKey = TransactionSortKey.EXPENSE_DATE,
        sortDirection = SortDirection.DESC)

    val response =
      TransactionsPageResponse(
        transactions =
          listOf(
            TransactionResponse.from(user1Transactions[5]),
            TransactionResponse.from(user1Transactions[3]),
            TransactionResponse.from(user1Transactions[1])),
        pageNumber = 0,
        totalItems = 3)

    mockMvc
      .get("/transactions?${request.toQueryString()}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(response), true) }
      }
  }

  @Test
  fun `search - with categories`() {
    val request =
      SearchTransactionsRequest(
        isCategorized = true,
        pageNumber = 0,
        pageSize = 100,
        sortKey = TransactionSortKey.EXPENSE_DATE,
        sortDirection = SortDirection.ASC)

    val response =
      TransactionsPageResponse(
        transactions =
          listOf(
            TransactionResponse.from(user1Transactions[0], user1Categories[0]),
            TransactionResponse.from(user1Transactions[2], user1Categories[2]),
            TransactionResponse.from(user1Transactions[4], user1Categories[1]),
            TransactionResponse.from(user1Transactions[6], user1Categories[0])),
        pageNumber = 0,
        totalItems = 4)

    mockMvc
      .get("/transactions?${request.toQueryString()}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(response), true) }
      }
  }

  @Test
  fun `search - with categories, but with more items than on the first page`() {
    val request =
      SearchTransactionsRequest(
        isCategorized = true,
        pageNumber = 0,
        pageSize = 2,
        sortKey = TransactionSortKey.EXPENSE_DATE,
        sortDirection = SortDirection.ASC)

    val response =
      TransactionsPageResponse(
        transactions =
          listOf(
            TransactionResponse.from(user1Transactions[0], user1Categories[0]),
            TransactionResponse.from(user1Transactions[2], user1Categories[2])),
        pageNumber = 0,
        totalItems = 4)

    mockMvc
      .get("/transactions?${request.toQueryString()}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(response), true) }
      }
  }

  @Test
  fun `search - with no categories`() {
    val request =
      SearchTransactionsRequest(
        isCategorized = false,
        pageNumber = 0,
        pageSize = 100,
        sortKey = TransactionSortKey.EXPENSE_DATE,
        sortDirection = SortDirection.ASC)

    val response =
      TransactionsPageResponse(
        transactions =
          listOf(
            TransactionResponse.from(user1Transactions[1]),
            TransactionResponse.from(user1Transactions[3]),
            TransactionResponse.from(user1Transactions[5])),
        pageNumber = 0,
        totalItems = 3)

    mockMvc
      .get("/transactions?${request.toQueryString()}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(response), true) }
      }
  }

  @Test
  fun `search - possible refunds`() {
    val txn1 =
      transactionRepository.saveAndFlush(
        user1Transactions[0].apply { amount = user1Transactions[0].amount * BigDecimal("-1") })
    val txn2 =
      transactionRepository.saveAndFlush(
        user1Transactions[2].apply { amount = user1Transactions[2].amount * BigDecimal("-1") })

    val request =
      SearchTransactionsRequest(
        isPossibleRefund = true,
        pageNumber = 0,
        pageSize = 100,
        sortKey = TransactionSortKey.EXPENSE_DATE,
        sortDirection = SortDirection.ASC)

    val response =
      TransactionsPageResponse(
        transactions =
          listOf(
            TransactionResponse.from(txn1, user1Categories[0]),
            TransactionResponse.from(txn2, user1Categories[2])),
        pageNumber = 0,
        totalItems = 2)

    mockMvc
      .get("/transactions/?${request.toQueryString()}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(response), true) }
      }
  }

  @Test
  fun `search - only duplicates`() {
    val txn1 = user1Transactions[0]
    val txn2 = transactionRepository.saveAndFlush(Transaction(txn1))
    entityManager.flushAndClear()

    val request =
      SearchTransactionsRequest(
        isDuplicate = true,
        pageNumber = 0,
        pageSize = 100,
        sortKey = TransactionSortKey.EXPENSE_DATE,
        sortDirection = SortDirection.ASC)

    val response =
      TransactionsPageResponse(
        transactions =
          listOf(
            TransactionResponse.from(txn1, user1Categories[0]).copy(duplicate = true),
            TransactionResponse.from(txn2, user1Categories[0]).copy(duplicate = true)),
        pageNumber = 0,
        totalItems = 2)

    mockMvc
      .get("/transactions?${request.toQueryString()}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(response), true) }
      }
  }

  @Test
  fun `search - invalid category options`() {
    val request =
      SearchTransactionsRequest(
        isCategorized = false,
        categoryIds = setOf(user1Categories[0].recordId),
        pageNumber = 0,
        pageSize = 100,
        sortKey = TransactionSortKey.EXPENSE_DATE,
        sortDirection = SortDirection.ASC)

    mockMvc
      .get("/transactions?${request.toQueryString()}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isBadRequest() } }
  }

  @Test
  fun `search - only non-duplicates`() {
    val txn1 = user1Transactions[0]
    transactionRepository.saveAndFlush(Transaction(txn1))
    val request =
      SearchTransactionsRequest(
        isDuplicate = false,
        pageNumber = 0,
        pageSize = 100,
        sortKey = TransactionSortKey.EXPENSE_DATE,
        sortDirection = SortDirection.ASC)

    val nonDuplicateIds = user1Transactions.subList(1, user1Transactions.size).map { it.recordId }
    val nonDuplicateTransactions =
      transactionViewRepository
        .findAllByRecordIdInAndUserId(nonDuplicateIds, 1L)
        .sortedWith(transactionComparator)

    val response =
      TransactionsPageResponse(
        transactions = nonDuplicateTransactions.map { TransactionResponse.from(it) },
        pageNumber = 0,
        totalItems = nonDuplicateTransactions.size.toLong())

    mockMvc
      .get("/transactions?${request.toQueryString()}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(response), true) }
      }
  }

  @Test
  fun `search - confirmed transactions only`() {
    user1Transactions =
      user1Transactions.map { txn ->
        transactionRepository.saveAndFlush(txn.apply { confirmed = false })
      }
    val txn1 =
      transactionRepository.saveAndFlush(user1Transactions.first().apply { confirmed = true })
    val txn2 = transactionRepository.saveAndFlush(user1Transactions[1].apply { confirmed = true })
    transactionRepository.saveAndFlush(user2Transactions.first().apply { confirmed = true })
    val request =
      SearchTransactionsRequest(
        isConfirmed = true,
        pageNumber = 0,
        pageSize = 100,
        sortKey = TransactionSortKey.EXPENSE_DATE,
        sortDirection = SortDirection.ASC)

    val response =
      TransactionsPageResponse(
        transactions =
          listOf(
            TransactionResponse.from(txn1, user1Categories.first()),
            TransactionResponse.from(txn2)),
        pageNumber = 0,
        totalItems = 2)

    mockMvc
      .get("/transactions?${request.toQueryString()}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(response), true) }
      }
  }

  @Test
  fun `search - unconfirmed transactions only`() {
    user1Transactions =
      user1Transactions.map { txn ->
        transactionRepository.saveAndFlush(txn.apply { confirmed = false })
      }
    transactionRepository.saveAndFlush(user1Transactions.first().apply { confirmed = true })
    transactionRepository.saveAndFlush(user1Transactions[1].apply { confirmed = true })
    val request =
      SearchTransactionsRequest(
        isConfirmed = false,
        pageNumber = 0,
        pageSize = 100,
        sortKey = TransactionSortKey.EXPENSE_DATE,
        sortDirection = SortDirection.ASC)

    val response =
      TransactionsPageResponse(
        transactions =
          listOf(
            TransactionResponse.from(user1Transactions[2], user1Categories[2]),
            TransactionResponse.from(user1Transactions[3]),
            TransactionResponse.from(user1Transactions[4], user1Categories[1]),
            TransactionResponse.from(user1Transactions[5]),
            TransactionResponse.from(user1Transactions[6], user1Categories[0])),
        pageNumber = 0,
        totalItems = 5)

    mockMvc
      .get("/transactions?${request.toQueryString()}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(response), true) }
      }
  }

  @Test
  fun `search - start and end dates`() {
    val expected =
      user1Transactions
        .mapIndexed { index, txn ->
          transactionRepository.saveAndFlush(
            txn.apply { expenseDate = LocalDate.now().minusDays(index.toLong()) })
        }
        .filter { it.expenseDate.isAfter(LocalDate.now().minusDays(3)) }
        .sortedWith(transactionComparator)

    val request =
      SearchTransactionsRequest(
        startDate = LocalDate.now().minusDays(2),
        endDate = LocalDate.now(),
        pageNumber = 0,
        pageSize = 100,
        sortKey = TransactionSortKey.EXPENSE_DATE,
        sortDirection = SortDirection.ASC)

    val response =
      TransactionsPageResponse(
        transactions =
          expected.map { txn ->
            TransactionResponse.from(txn, txn.categoryId?.let { user1CategoriesMap[it] })
          },
        pageNumber = 0,
        totalItems = expected.size.toLong())

    mockMvc
      .get("/transactions?${request.toQueryString()}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(response), true) }
      }
  }

  @Test
  fun `search - categories`() {
    val user2Cat = dataHelper.createCategory(2L, "User2 Cat")
    transactionRepository.saveAndFlush(
      user2Transactions.first().apply { categoryId = user2Cat.recordId })

    val categories = setOf(user1Categories.first().recordId, user2Cat.recordId)

    val response =
      TransactionsPageResponse(
        transactions =
          listOf(
            TransactionResponse.from(user1Transactions.first(), user1Categories.first()),
            TransactionResponse.from(user1Transactions.last(), user1Categories.first())),
        pageNumber = 0,
        totalItems = 2)

    val request =
      SearchTransactionsRequest(
        categoryIds = categories,
        pageNumber = 0,
        pageSize = 100,
        sortKey = TransactionSortKey.EXPENSE_DATE,
        sortDirection = SortDirection.ASC)

    mockMvc
      .get("/transactions?${request.toQueryString()}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(response), true) }
      }
  }

  @Test
  fun deleteTransactions() {
    val request =
      DeleteTransactionsRequest(
        ids = setOf(user1Transactions.first().recordId, user2Transactions.first().recordId))

    mockMvc
      .delete("/transactions") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isNoContent() } }

    entityManager.flushAndClear()

    assertThat(transactionRepository.findById(user1Transactions.first().recordId)).isEmpty
    assertThat(transactionRepository.findById(user2Transactions.first().recordId)).isPresent
  }

  @Test
  fun categorizeTransactions_removeCategory() {
    assertThat(user1Transactions[0].categoryId).isNotNull
    val request =
      CategorizeTransactionsRequest(
        transactionsAndCategories =
          setOf(TransactionAndCategory(user1Transactions[0].recordId, null)))

    mockMvc
      .put("/transactions/categorize") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isNoContent() } }

    entityManager.flushAndClear()

    assertThat(transactionRepository.findById(user1Transactions[0].recordId))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", null)
      .hasFieldOrPropertyWithValue("confirmed", true)
  }

  @Test
  fun confirmTransactions() {
    val request =
      ConfirmTransactionsRequest(
        transactionsToConfirm =
          setOf(
            TransactionToConfirm(transactionId = user1Transactions[0].recordId, confirmed = true),
            TransactionToConfirm(transactionId = user2Transactions[0].recordId, confirmed = true)))

    mockMvc
      .put("/transactions/confirm") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isNoContent() } }

    assertThat(transactionRepository.findById(user1Transactions[0].recordId))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("confirmed", true)
    assertThat(transactionRepository.findById(user2Transactions[0].recordId))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("confirmed", false)
  }

  @Test
  fun categorizeTransactions() {
    val user2Category = dataHelper.createCategory(2L, "Other")
    val uncategorizedTransaction = user1Transactions[5]
    assertThat(uncategorizedTransaction.categoryId).isNull()

    val categorizedTransaction = user1Transactions[4]
    assertThat(categorizedTransaction.categoryId)
      .isNotNull
      .isNotEqualTo(user1Categories.first().recordId)

    val request =
      CategorizeTransactionsRequest(
        transactionsAndCategories =
          setOf(
            TransactionAndCategory(
              uncategorizedTransaction.recordId, user1Categories.first().recordId),
            TransactionAndCategory(
              categorizedTransaction.recordId, user1Categories.first().recordId),
            TransactionAndCategory(
              user2Transactions.first().recordId, user1Categories.first().recordId),
            TransactionAndCategory(user1Transactions[2].recordId, user2Category.recordId)))

    entityManager.flushAndClear()

    mockMvc
      .put("/transactions/categorize") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isNoContent() } }

    entityManager.flushAndClear()

    assertThat(transactionRepository.findById(uncategorizedTransaction.recordId))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", user1Categories.first().recordId)
      .hasFieldOrPropertyWithValue("confirmed", false)
    assertThat(transactionRepository.findById(categorizedTransaction.recordId))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", user1Categories.first().recordId)
      .hasFieldOrPropertyWithValue("confirmed", true)
    assertThat(transactionRepository.findById(user2Transactions.first().recordId))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", null)
      .hasFieldOrPropertyWithValue("confirmed", false)
    assertThat(transactionRepository.findById(user1Transactions[2].recordId))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", null)
      .hasFieldOrPropertyWithValue("confirmed", true)
  }

  @Test
  fun createTransaction() {
    val request =
      CreateTransactionRequest(
        expenseDate = LocalDate.of(2022, 1, 1),
        description = "Another Transaction",
        amount = BigDecimal("-120"),
        categoryId = user1Categories[0].recordId)

    val responseString =
      mockMvc
        .post("/transactions") {
          secure = true
          header("Authorization", "Bearer $token")
          content = objectMapper.writeValueAsString(request)
          contentType = MediaType.APPLICATION_JSON
        }
        .andExpect { status { isOk() } }
        .andReturn()
        .response
        .contentAsString
    val response = objectMapper.readValue(responseString, TransactionResponse::class.java)

    assertThat(response)
      .hasFieldOrPropertyWithValue("expenseDate", request.expenseDate)
      .hasFieldOrPropertyWithValue("amount", request.amount)
      .hasFieldOrPropertyWithValue("description", request.description)
      .hasFieldOrPropertyWithValue("categoryId", request.categoryId)
      .hasFieldOrPropertyWithValue("categoryName", user1Categories[0].name)
      .hasFieldOrPropertyWithValue("confirmed", true)
      .hasFieldOrPropertyWithValue("duplicate", false)

    val dbTransaction = transactionRepository.findById(response.id).orElseThrow()
    assertThat(dbTransaction)
      .hasFieldOrPropertyWithValue("expenseDate", request.expenseDate)
      .hasFieldOrPropertyWithValue("amount", request.amount)
      .hasFieldOrPropertyWithValue("description", request.description)
      .hasFieldOrPropertyWithValue("categoryId", request.categoryId)
      .hasFieldOrPropertyWithValue("confirmed", true)
  }

  @Test
  fun createTransaction_invalidCategoryId() {
    val user2Category = dataHelper.createCategory(2L, "Other")
    val request =
      CreateTransactionRequest(
        expenseDate = LocalDate.of(2022, 1, 1),
        description = "Another Transaction",
        amount = BigDecimal("-120"),
        categoryId = user2Category.recordId)

    val responseString =
      mockMvc
        .post("/transactions") {
          secure = true
          header("Authorization", "Bearer $token")
          content = objectMapper.writeValueAsString(request)
          contentType = MediaType.APPLICATION_JSON
        }
        .andExpect { status { isOk() } }
        .andReturn()
        .response
        .contentAsString
    val response = objectMapper.readValue(responseString, TransactionResponse::class.java)

    assertThat(response)
      .hasFieldOrPropertyWithValue("expenseDate", request.expenseDate)
      .hasFieldOrPropertyWithValue("amount", request.amount)
      .hasFieldOrPropertyWithValue("description", request.description)
      .hasFieldOrPropertyWithValue("categoryId", null)
      .hasFieldOrPropertyWithValue("categoryName", null)
      .hasFieldOrPropertyWithValue("confirmed", true)
      .hasFieldOrPropertyWithValue("duplicate", false)

    val dbTransaction = transactionRepository.findById(response.id).orElseThrow()
    assertThat(dbTransaction)
      .hasFieldOrPropertyWithValue("expenseDate", request.expenseDate)
      .hasFieldOrPropertyWithValue("amount", request.amount)
      .hasFieldOrPropertyWithValue("description", request.description)
      .hasFieldOrPropertyWithValue("categoryId", null)
      .hasFieldOrPropertyWithValue("confirmed", true)
  }

  @Test
  fun updateTransactionDetails() {
    val transactionId = user1Transactions[0].recordId
    val request =
      UpdateTransactionDetailsRequest(
        transactionId = transactionId,
        confirmed = true,
        expenseDate = LocalDate.of(1990, 1, 1),
        description = "New Description",
        amount = BigDecimal("-112.57"),
        categoryId = user1Categories[0].recordId)

    mockMvc
      .put("/transactions/$transactionId/details") {
        secure = true
        content = objectMapper.writeValueAsString(request)
        contentType = MediaType.APPLICATION_JSON
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isNoContent() } }

    val dbTransaction = transactionRepository.findById(transactionId).orElseThrow()
    assertThat(dbTransaction)
      .hasFieldOrPropertyWithValue("confirmed", request.confirmed)
      .hasFieldOrPropertyWithValue("expenseDate", request.expenseDate)
      .hasFieldOrPropertyWithValue("description", request.description)
      .hasFieldOrPropertyWithValue("categoryId", request.categoryId)
    assertThat(dbTransaction.amount.toDouble()).isEqualTo(request.amount.toDouble())
  }

  @Test
  fun updateTransactionDetails_invalidCategoryId() {
    val user2Category = dataHelper.createCategory(2L, "Other")
    val transactionId = user1Transactions[0].recordId
    val request =
      UpdateTransactionDetailsRequest(
        transactionId = transactionId,
        confirmed = true,
        expenseDate = LocalDate.of(1990, 1, 1),
        description = "New Description",
        amount = BigDecimal("-112.57"),
        categoryId = user2Category.recordId)

    mockMvc
      .put("/transactions/$transactionId/details") {
        secure = true
        content = objectMapper.writeValueAsString(request)
        contentType = MediaType.APPLICATION_JSON
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isNoContent() } }

    val dbTransaction = transactionRepository.findById(transactionId).orElseThrow()
    assertThat(dbTransaction)
      .hasFieldOrPropertyWithValue("confirmed", request.confirmed)
      .hasFieldOrPropertyWithValue("expenseDate", request.expenseDate)
      .hasFieldOrPropertyWithValue("description", request.description)
      .hasFieldOrPropertyWithValue("categoryId", null)
    assertThat(dbTransaction.amount.toDouble()).isEqualTo(request.amount.toDouble())
  }

  @Test
  fun updateTransactions() {
    // This does also validate a category id that's for the wrong user
    val user2Category = dataHelper.createCategory(2L, "Other")
    val uncategorizedTransaction = user1Transactions[5]
    assertThat(uncategorizedTransaction.categoryId).isNull()

    assertThat(user1Transactions[1]).hasFieldOrPropertyWithValue("confirmed", false)

    val categorizedTransaction = user1Transactions[4]

    val request =
      UpdateTransactionsRequest(
        transactions =
          setOf(
            TransactionToUpdate(
              transactionId = uncategorizedTransaction.recordId,
              categoryId = user1Categories.first().recordId,
              confirmed = false),
            TransactionToUpdate(
              transactionId = categorizedTransaction.recordId,
              categoryId = user1Categories.first().recordId,
              confirmed = false),
            TransactionToUpdate(
              transactionId = user2Transactions.first().recordId,
              categoryId = user1Categories.first().recordId,
              confirmed = false),
            TransactionToUpdate(
              transactionId = user1Transactions[2].recordId,
              categoryId = user2Category.recordId,
              confirmed = false),
            TransactionToUpdate(transactionId = user1Transactions[6].recordId, confirmed = true),
            TransactionToUpdate(transactionId = user2Transactions[0].recordId, confirmed = true)))

    mockMvc
      .put("/transactions") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isNoContent() } }

    assertThat(transactionRepository.findById(uncategorizedTransaction.recordId))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", user1Categories.first().recordId)
      .hasFieldOrPropertyWithValue("confirmed", false)
    assertThat(transactionRepository.findById(categorizedTransaction.recordId))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", user1Categories.first().recordId)
      .hasFieldOrPropertyWithValue("confirmed", false)
    assertThat(transactionRepository.findById(user2Transactions.first().recordId))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", null)
      .hasFieldOrPropertyWithValue("confirmed", false)
    assertThat(transactionRepository.findById(user1Transactions[2].recordId))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", null)
      .hasFieldOrPropertyWithValue("confirmed", false)

    assertThat(transactionRepository.findById(user1Transactions[6].recordId))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("confirmed", true)
  }

  @Test
  fun `createTransaction is duplicate`() {
    val request =
      CreateTransactionRequest(
        expenseDate = user1Transactions[0].expenseDate,
        description = user1Transactions[0].description,
        amount = user1Transactions[0].amount,
        categoryId = user1Transactions[0].categoryId)

    val responseString =
      mockMvc
        .post("/transactions") {
          secure = true
          header("Authorization", "Bearer $token")
          content = objectMapper.writeValueAsString(request)
          contentType = MediaType.APPLICATION_JSON
        }
        .andExpect { status { isOk() } }
        .andReturn()
        .response
        .contentAsString
    val response = objectMapper.readValue(responseString, TransactionResponse::class.java)
    assertThat(response)
      .hasFieldOrPropertyWithValue("expenseDate", request.expenseDate)
      .hasFieldOrPropertyWithValue("description", request.description)
      .hasFieldOrPropertyWithValue("amount", request.amount)
      .hasFieldOrPropertyWithValue("categoryId", request.categoryId)
      .hasFieldOrPropertyWithValue("duplicate", true)
  }

  @Test
  fun `updateTransactionDetails is duplicate`() {
    val request =
      UpdateTransactionDetailsRequest(
        transactionId = user1Transactions[1].recordId,
        confirmed = user1Transactions[0].confirmed,
        expenseDate = user1Transactions[0].expenseDate,
        description = user1Transactions[0].description,
        amount = user1Transactions[0].amount,
        categoryId = user1Transactions[0].categoryId)

    mockMvc
      .put("/transactions/${request.transactionId}/details") {
        secure = true
        header("Authorization", "Bearer $token")
        content = objectMapper.writeValueAsString(request)
        contentType = MediaType.APPLICATION_JSON
      }
      .andExpect { status { isNoContent() } }

    entityManager.flushAndClear()

    val transaction = transactionViewRepository.findById(request.transactionId).orElseThrow()
    assertThat(transaction).hasFieldOrPropertyWithValue("duplicate", true)
  }

  @Test
  fun `getPossibleDuplicates - wrong user id`() {
    val txn1 = user2Transactions[0]
    transactionRepository.saveAndFlush(Transaction(txn1))
    transactionRepository.saveAndFlush(Transaction(txn1))
    entityManager.flushAndClear()

    val response =
      TransactionDuplicatePageResponse(transactions = listOf(), totalItems = 0, pageNumber = 0)

    mockMvc
      .get("/transactions/${txn1.recordId}/duplicates?pageNumber=0&pageSize=25") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(response), true) }
      }
  }

  @Test
  fun `getPossibleDuplicates - has duplicates`() {
    val txn1 = user1Transactions[0]
    val txn2 = transactionRepository.saveAndFlush(Transaction(txn1))
    val txn3 = transactionRepository.saveAndFlush(Transaction(txn1))
    entityManager.flushAndClear()

    val expectedTransactions =
      transactionViewRepository
        .findAllByRecordIdInAndUserId(listOf(txn3.recordId, txn2.recordId), 1L)
        .map { TransactionDuplicateResponse.from(it) }

    val response =
      TransactionDuplicatePageResponse(
        transactions = expectedTransactions,
        totalItems = expectedTransactions.size.toLong(),
        pageNumber = 0)

    mockMvc
      .get("/transactions/${txn1.recordId}/duplicates?pageNumber=0&pageSize=25") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(response), true) }
      }
  }

  @Test
  fun `getPossibleDuplicates - no duplicates`() {
    val response =
      TransactionDuplicatePageResponse(transactions = listOf(), pageNumber = 0, totalItems = 0)
    mockMvc
      .get("/transactions/${user1Transactions[0].recordId}/duplicates?pageNumber=0&pageSize=25") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(response)) }
      }
  }

  @Test
  fun markNotDuplicate() {
    val txn1 = user1Transactions[0]
    val txn2 = transactionRepository.saveAndFlush(Transaction(txn1))
    val txn3 = transactionRepository.saveAndFlush(Transaction(txn1))
    entityManager.flushAndClear()

    mockMvc
      .put("/transactions/${txn1.recordId}/notDuplicate") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isNoContent() } }

    entityManager.flushAndClear()

    val txn1Duplicates =
      transactionViewRepository.findAllDuplicates(txn1.recordId, 1L, PageRequest.of(0, 25))
    assertThat(txn1Duplicates).isEmpty()
    val txn2Duplicates =
      transactionViewRepository.findAllDuplicates(txn2.recordId, 1L, PageRequest.of(0, 25))
    assertThat(txn2Duplicates).hasSize(1).extracting("recordId").contains(txn3.recordId)
  }

  @Test
  fun `categorizeTransactions - clears last rule applied`() {
    dataHelper.createLastRuleApplied(1L, user1Transactions[0].recordId, rule.recordId)
    dataHelper.createLastRuleApplied(1L, user1Transactions[1].recordId, rule.recordId)
    dataHelper.createLastRuleApplied(1L, user1Transactions[2].recordId, rule.recordId)
    val request =
      CategorizeTransactionsRequest(
        transactionsAndCategories =
          setOf(
            TransactionAndCategory(user1Transactions[0].recordId, user1Categories[1].recordId),
            TransactionAndCategory(user1Transactions[1].recordId, null),
            TransactionAndCategory(user1Transactions[2].recordId, user1Transactions[2].categoryId)))

    mockMvc
      .put("/transactions/categorize") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isNoContent() } }

    entityManager.flushAndClear()

    assertThat(
        lastRuleAppliedRepository.findByUserIdAndTransactionId(1L, user1Transactions[0].recordId))
      .isNull()
    assertThat(
        lastRuleAppliedRepository.findByUserIdAndTransactionId(1L, user1Transactions[1].recordId))
      .isNotNull
    assertThat(
        lastRuleAppliedRepository.findByUserIdAndTransactionId(1L, user1Transactions[2].recordId))
      .isNotNull
  }

  @Test
  fun `confirmTransactions - clears last rule applied`() {
    transactionRepository.saveAndFlush(user1Transactions[0].apply { confirmed = false })
    transactionRepository.saveAndFlush(user1Transactions[1].apply { confirmed = false })
    dataHelper.createLastRuleApplied(1L, user1Transactions[0].recordId, rule.recordId)
    dataHelper.createLastRuleApplied(1L, user1Transactions[1].recordId, rule.recordId)
    val request =
      ConfirmTransactionsRequest(
        transactionsToConfirm =
          setOf(
            TransactionToConfirm(transactionId = user1Transactions[0].recordId, confirmed = true),
            TransactionToConfirm(transactionId = user1Transactions[1].recordId, confirmed = false)))

    mockMvc
      .put("/transactions/confirm") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isNoContent() } }

    assertThat(
        lastRuleAppliedRepository.findByUserIdAndTransactionId(1L, user1Transactions[0].recordId))
      .isNull()
    assertThat(
        lastRuleAppliedRepository.findByUserIdAndTransactionId(1L, user1Transactions[1].recordId))
      .isNotNull
  }

  @Test
  fun `updateTransactions - confirming and or setting category clears last rule applied`() {
    val transactionToCategorize =
      TransactionToUpdate(
        transactionId = user1Transactions[0].recordId,
        categoryId = user1Categories.first().recordId,
        confirmed = false)
    val transactionToConfirm =
      TransactionToUpdate(
        transactionId = user1Transactions[1].recordId, categoryId = null, confirmed = true)
    val transactionToDoNothing =
      TransactionToUpdate(
        transactionId = user1Transactions[3].recordId, categoryId = null, confirmed = false)
    dataHelper.createLastRuleApplied(1L, transactionToCategorize.transactionId, rule.recordId)
    dataHelper.createLastRuleApplied(1L, transactionToConfirm.transactionId, rule.recordId)
    dataHelper.createLastRuleApplied(1L, transactionToDoNothing.transactionId, rule.recordId)
    val request =
      UpdateTransactionsRequest(
        transactions = setOf(transactionToCategorize, transactionToConfirm, transactionToDoNothing))

    mockMvc
      .put("/transactions") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isNoContent() } }

    assertThat(
        lastRuleAppliedRepository.findByUserIdAndTransactionId(
          1L, transactionToCategorize.transactionId))
      .isNull()
    assertThat(
        lastRuleAppliedRepository.findByUserIdAndTransactionId(
          1L, transactionToConfirm.transactionId))
      .isNull()
    assertThat(
        lastRuleAppliedRepository.findByUserIdAndTransactionId(
          1L, transactionToDoNothing.transactionId))
      .isNotNull
  }

  @Test
  fun `updateTransactionDetails - confirming and or setting category clears last rule applied`() {
    transactionRepository.saveAndFlush(
      user1Transactions[0].apply {
        categoryId = null
        confirmed = false
      })
    transactionRepository.saveAndFlush(
      user1Transactions[1].apply {
        categoryId = null
        confirmed = false
      })
    transactionRepository.saveAndFlush(
      user1Transactions[2].apply {
        categoryId = null
        confirmed = false
      })
    dataHelper.createLastRuleApplied(1L, user1Transactions[0].recordId, rule.recordId)
    dataHelper.createLastRuleApplied(1L, user1Transactions[1].recordId, rule.recordId)
    dataHelper.createLastRuleApplied(1L, user1Transactions[2].recordId, rule.recordId)

    val doUpdate: (TypedId<TransactionId>, TypedId<CategoryId>?, Boolean) -> Unit =
      { transactionId, categoryId, confirmed ->
        val request =
          UpdateTransactionDetailsRequest(
            transactionId = transactionId,
            confirmed = confirmed,
            expenseDate = LocalDate.of(1990, 1, 1),
            description = "New Description",
            amount = BigDecimal("-112.57"),
            categoryId = categoryId)

        mockMvc
          .put("/transactions/$transactionId/details") {
            secure = true
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer $token")
          }
          .andExpect { status { isNoContent() } }
      }

    // Categorize
    doUpdate(user1Transactions[0].recordId, user1Categories[0].recordId, false)
    // Confirm
    doUpdate(user1Transactions[1].recordId, null, true)
    // Neither
    doUpdate(user1Transactions[2].recordId, null, false)

    entityManager.flushAndClear()

    assertThat(
        lastRuleAppliedRepository.findByUserIdAndTransactionId(1L, user1Transactions[0].recordId))
      .isNull()
    assertThat(
        lastRuleAppliedRepository.findByUserIdAndTransactionId(1L, user1Transactions[1].recordId))
      .isNull()
    assertThat(
        lastRuleAppliedRepository.findByUserIdAndTransactionId(1L, user1Transactions[2].recordId))
      .isNotNull
  }

  @Test
  fun `deleteTransactions - clears last rule applied`() {
    dataHelper.createLastRuleApplied(1L, user1Transactions[0].recordId, rule.recordId)
    val request = DeleteTransactionsRequest(ids = setOf(user1Transactions[0].recordId))

    mockMvc
      .delete("/transactions") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isNoContent() } }

    assertThat(
        lastRuleAppliedRepository.findByUserIdAndTransactionId(1L, user1Transactions[0].recordId))
      .isNull()
  }

  @Test
  fun `markNotDuplicate - different user id`() {
    val txn1 = user2Transactions[0]
    transactionRepository.saveAndFlush(Transaction(txn1))
    transactionRepository.saveAndFlush(Transaction(txn1))
    entityManager.flushAndClear()

    mockMvc
      .put("/transactions/${txn1.recordId}/notDuplicate") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isNoContent() } }

    entityManager.flushAndClear()

    val txn1Duplicates =
      transactionViewRepository.findAllDuplicates(txn1.recordId, 2L, PageRequest.of(0, 25))
    assertThat(txn1Duplicates).hasSize(2)
  }

  @Test
  fun getTransactionDetails() {
    val txn = user1Transactions[0]
    val responseString =
      mockMvc
        .get("/transactions/${txn.recordId}/details") {
          secure = true
          header("Authorization", "Bearer $token")
        }
        .andExpect { status { isOk() } }
        .andReturn()
        .response
        .contentAsString
    val response = objectMapper.readValue(responseString, TransactionDetailsResponse::class.java)
    val expected =
      transactionViewRepository.findById(txn.recordId).orElseThrow().let {
        TransactionDetailsResponse.from(it)
      }
    assertEquals(expected, response)
  }

  @Test
  fun `getTransactionDetails - user does not have access`() {
    val txn = user2Transactions[0]
    mockMvc
      .get("/transactions/${txn.recordId}/details") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isBadRequest() } }
  }

  @Test
  fun `getTransactionDetails - does not exist`() {
    mockMvc
      .get("/transactions/${UUID.randomUUID()}/details") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isBadRequest() } }
  }
}
