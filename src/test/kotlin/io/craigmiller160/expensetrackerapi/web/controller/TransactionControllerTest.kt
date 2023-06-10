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
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import io.craigmiller160.expensetrackerapi.testutils.DefaultUsers
import io.craigmiller160.expensetrackerapi.testutils.userTypedId
import io.craigmiller160.expensetrackerapi.web.types.*
import io.craigmiller160.expensetrackerapi.web.types.transaction.*
import jakarta.persistence.EntityManager
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import java.util.Comparator
import java.util.UUID
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
    private val entityManager: EntityManager,
    private val defaultUsers: DefaultUsers
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
    rule = dataHelper.createRule(defaultUsers.primaryUser.userTypedId, user1Categories[0].uid)
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
            categoryIds = setOf(user1Categories[0].uid),
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

    val nonDuplicateIds = user1Transactions.subList(1, user1Transactions.size).map { it.uid }
    val nonDuplicateTransactions =
        transactionViewRepository
            .findAllByUidInAndUserId(nonDuplicateIds, defaultUsers.primaryUser.userTypedId)
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
    val user2Cat = dataHelper.createCategory(defaultUsers.secondaryUser.userTypedId, "User2 Cat")
    transactionRepository.saveAndFlush(
        user2Transactions.first().apply { categoryId = user2Cat.uid })

    val categories = setOf(user1Categories.first().uid, user2Cat.uid)

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
            ids = setOf(user1Transactions.first().uid, user2Transactions.first().uid))

    mockMvc
        .delete("/transactions") {
          secure = true
          header("Authorization", "Bearer $token")
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect { status { isNoContent() } }

    entityManager.flushAndClear()

    assertThat(transactionRepository.findById(user1Transactions.first().uid)).isEmpty
    assertThat(transactionRepository.findById(user2Transactions.first().uid)).isPresent
  }

  @Test
  fun categorizeTransactions_removeCategory() {
    assertThat(user1Transactions[0].categoryId).isNotNull
    val request =
        CategorizeTransactionsRequest(
            transactionsAndCategories =
                setOf(TransactionAndCategory(user1Transactions[0].uid, null)))

    mockMvc
        .put("/transactions/categorize") {
          secure = true
          header("Authorization", "Bearer $token")
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect { status { isNoContent() } }

    entityManager.flushAndClear()

    assertThat(transactionRepository.findById(user1Transactions[0].uid))
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
                    TransactionToConfirm(
                        transactionId = user1Transactions[0].uid, confirmed = true),
                    TransactionToConfirm(
                        transactionId = user2Transactions[0].uid, confirmed = true)))

    mockMvc
        .put("/transactions/confirm") {
          secure = true
          header("Authorization", "Bearer $token")
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect { status { isNoContent() } }

    assertThat(transactionRepository.findById(user1Transactions[0].uid))
        .isPresent
        .get()
        .hasFieldOrPropertyWithValue("confirmed", true)
    assertThat(transactionRepository.findById(user2Transactions[0].uid))
        .isPresent
        .get()
        .hasFieldOrPropertyWithValue("confirmed", false)
  }

  @Test
  fun categorizeTransactions() {
    val user2Category = dataHelper.createCategory(defaultUsers.secondaryUser.userTypedId, "Other")
    val uncategorizedTransaction = user1Transactions[5]
    assertThat(uncategorizedTransaction.categoryId).isNull()

    val categorizedTransaction = user1Transactions[4]
    assertThat(categorizedTransaction.categoryId)
        .isNotNull
        .isNotEqualTo(user1Categories.first().uid)

    val request =
        CategorizeTransactionsRequest(
            transactionsAndCategories =
                setOf(
                    TransactionAndCategory(
                        uncategorizedTransaction.uid, user1Categories.first().uid),
                    TransactionAndCategory(categorizedTransaction.uid, user1Categories.first().uid),
                    TransactionAndCategory(
                        user2Transactions.first().uid, user1Categories.first().uid),
                    TransactionAndCategory(user1Transactions[2].uid, user2Category.uid)))

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

    assertThat(transactionRepository.findById(uncategorizedTransaction.uid))
        .isPresent
        .get()
        .hasFieldOrPropertyWithValue("categoryId", user1Categories.first().uid)
        .hasFieldOrPropertyWithValue("confirmed", false)
    assertThat(transactionRepository.findById(categorizedTransaction.uid))
        .isPresent
        .get()
        .hasFieldOrPropertyWithValue("categoryId", user1Categories.first().uid)
        .hasFieldOrPropertyWithValue("confirmed", true)
    assertThat(transactionRepository.findById(user2Transactions.first().uid))
        .isPresent
        .get()
        .hasFieldOrPropertyWithValue("categoryId", null)
        .hasFieldOrPropertyWithValue("confirmed", false)
    assertThat(transactionRepository.findById(user1Transactions[2].uid))
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
            categoryId = user1Categories[0].uid)

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
    val user2Category = dataHelper.createCategory(defaultUsers.secondaryUser.userTypedId, "Other")
    val request =
        CreateTransactionRequest(
            expenseDate = LocalDate.of(2022, 1, 1),
            description = "Another Transaction",
            amount = BigDecimal("-120"),
            categoryId = user2Category.uid)

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
    val transactionId = user1Transactions[0].uid
    val request =
        UpdateTransactionDetailsRequest(
            transactionId = transactionId,
            confirmed = true,
            expenseDate = LocalDate.of(1990, 1, 1),
            description = "New Description",
            amount = BigDecimal("-112.57"),
            categoryId = user1Categories[0].uid)

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
    val user2Category = dataHelper.createCategory(defaultUsers.secondaryUser.userTypedId, "Other")
    val transactionId = user1Transactions[0].uid
    val request =
        UpdateTransactionDetailsRequest(
            transactionId = transactionId,
            confirmed = true,
            expenseDate = LocalDate.of(1990, 1, 1),
            description = "New Description",
            amount = BigDecimal("-112.57"),
            categoryId = user2Category.uid)

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
    val user2Category = dataHelper.createCategory(defaultUsers.secondaryUser.userTypedId, "Other")
    val uncategorizedTransaction = user1Transactions[5]
    assertThat(uncategorizedTransaction.categoryId).isNull()

    assertThat(user1Transactions[1]).hasFieldOrPropertyWithValue("confirmed", false)

    val categorizedTransaction = user1Transactions[4]

    val request =
        UpdateTransactionsRequest(
            transactions =
                setOf(
                    TransactionToUpdate(
                        transactionId = uncategorizedTransaction.uid,
                        categoryId = user1Categories.first().uid,
                        confirmed = false),
                    TransactionToUpdate(
                        transactionId = categorizedTransaction.uid,
                        categoryId = user1Categories.first().uid,
                        confirmed = false),
                    TransactionToUpdate(
                        transactionId = user2Transactions.first().uid,
                        categoryId = user1Categories.first().uid,
                        confirmed = false),
                    TransactionToUpdate(
                        transactionId = user1Transactions[2].uid,
                        categoryId = user2Category.uid,
                        confirmed = false),
                    TransactionToUpdate(transactionId = user1Transactions[6].uid, confirmed = true),
                    TransactionToUpdate(
                        transactionId = user2Transactions[0].uid, confirmed = true)))

    mockMvc
        .put("/transactions") {
          secure = true
          header("Authorization", "Bearer $token")
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect { status { isNoContent() } }

    assertThat(transactionRepository.findById(uncategorizedTransaction.uid))
        .isPresent
        .get()
        .hasFieldOrPropertyWithValue("categoryId", user1Categories.first().uid)
        .hasFieldOrPropertyWithValue("confirmed", false)
    assertThat(transactionRepository.findById(categorizedTransaction.uid))
        .isPresent
        .get()
        .hasFieldOrPropertyWithValue("categoryId", user1Categories.first().uid)
        .hasFieldOrPropertyWithValue("confirmed", false)
    assertThat(transactionRepository.findById(user2Transactions.first().uid))
        .isPresent
        .get()
        .hasFieldOrPropertyWithValue("categoryId", null)
        .hasFieldOrPropertyWithValue("confirmed", false)
    assertThat(transactionRepository.findById(user1Transactions[2].uid))
        .isPresent
        .get()
        .hasFieldOrPropertyWithValue("categoryId", null)
        .hasFieldOrPropertyWithValue("confirmed", false)

    assertThat(transactionRepository.findById(user1Transactions[6].uid))
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
            transactionId = user1Transactions[1].uid,
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
        .get("/transactions/${txn1.uid}/duplicates?pageNumber=0&pageSize=25") {
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
            .findAllByUidInAndUserId(
                listOf(txn3.uid, txn2.uid), defaultUsers.primaryUser.userTypedId)
            .sortedByDescending { it.updated }
            .map { TransactionDuplicateResponse.from(it) }

    val response =
        TransactionDuplicatePageResponse(
            transactions = expectedTransactions,
            totalItems = expectedTransactions.size.toLong(),
            pageNumber = 0)

    mockMvc
        .get("/transactions/${txn1.uid}/duplicates?pageNumber=0&pageSize=25") {
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
        .get("/transactions/${user1Transactions[0].uid}/duplicates?pageNumber=0&pageSize=25") {
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
        .put("/transactions/${txn1.uid}/notDuplicate") {
          secure = true
          header("Authorization", "Bearer $token")
        }
        .andExpect { status { isNoContent() } }

    entityManager.flushAndClear()

    val txn1Duplicates =
        transactionViewRepository.findAllDuplicates(
            txn1.uid, defaultUsers.primaryUser.userTypedId, PageRequest.of(0, 25))
    assertThat(txn1Duplicates).isEmpty()
    val txn2Duplicates =
        transactionViewRepository.findAllDuplicates(
            txn2.uid, defaultUsers.primaryUser.userTypedId, PageRequest.of(0, 25))
    assertThat(txn2Duplicates).hasSize(1).extracting("uid").contains(txn3.uid)
  }

  @Test
  fun `categorizeTransactions - clears last rule applied`() {
    dataHelper.createLastRuleApplied(
        defaultUsers.primaryUser.userTypedId, user1Transactions[0].uid, rule.uid)
    dataHelper.createLastRuleApplied(
        defaultUsers.primaryUser.userTypedId, user1Transactions[1].uid, rule.uid)
    dataHelper.createLastRuleApplied(
        defaultUsers.primaryUser.userTypedId, user1Transactions[2].uid, rule.uid)
    val request =
        CategorizeTransactionsRequest(
            transactionsAndCategories =
                setOf(
                    TransactionAndCategory(user1Transactions[0].uid, user1Categories[1].uid),
                    TransactionAndCategory(user1Transactions[1].uid, null),
                    TransactionAndCategory(
                        user1Transactions[2].uid, user1Transactions[2].categoryId)))

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
            lastRuleAppliedRepository.findByUserIdAndTransactionId(
                defaultUsers.primaryUser.userTypedId, user1Transactions[0].uid))
        .isNull()
    assertThat(
            lastRuleAppliedRepository.findByUserIdAndTransactionId(
                defaultUsers.primaryUser.userTypedId, user1Transactions[1].uid))
        .isNotNull
    assertThat(
            lastRuleAppliedRepository.findByUserIdAndTransactionId(
                defaultUsers.primaryUser.userTypedId, user1Transactions[2].uid))
        .isNotNull
  }

  @Test
  fun `confirmTransactions - clears last rule applied`() {
    transactionRepository.saveAndFlush(user1Transactions[0].apply { confirmed = false })
    transactionRepository.saveAndFlush(user1Transactions[1].apply { confirmed = false })
    dataHelper.createLastRuleApplied(
        defaultUsers.primaryUser.userTypedId, user1Transactions[0].uid, rule.uid)
    dataHelper.createLastRuleApplied(
        defaultUsers.primaryUser.userTypedId, user1Transactions[1].uid, rule.uid)
    val request =
        ConfirmTransactionsRequest(
            transactionsToConfirm =
                setOf(
                    TransactionToConfirm(
                        transactionId = user1Transactions[0].uid, confirmed = true),
                    TransactionToConfirm(
                        transactionId = user1Transactions[1].uid, confirmed = false)))

    mockMvc
        .put("/transactions/confirm") {
          secure = true
          header("Authorization", "Bearer $token")
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect { status { isNoContent() } }

    assertThat(
            lastRuleAppliedRepository.findByUserIdAndTransactionId(
                defaultUsers.primaryUser.userTypedId, user1Transactions[0].uid))
        .isNull()
    assertThat(
            lastRuleAppliedRepository.findByUserIdAndTransactionId(
                defaultUsers.primaryUser.userTypedId, user1Transactions[1].uid))
        .isNotNull
  }

  @Test
  fun `updateTransactions - confirming and or setting category clears last rule applied`() {
    val transactionToCategorize =
        TransactionToUpdate(
            transactionId = user1Transactions[0].uid,
            categoryId = user1Categories.first().uid,
            confirmed = false)
    val transactionToConfirm =
        TransactionToUpdate(
            transactionId = user1Transactions[1].uid, categoryId = null, confirmed = true)
    val transactionToDoNothing =
        TransactionToUpdate(
            transactionId = user1Transactions[3].uid, categoryId = null, confirmed = false)
    dataHelper.createLastRuleApplied(
        defaultUsers.primaryUser.userTypedId, transactionToCategorize.transactionId, rule.uid)
    dataHelper.createLastRuleApplied(
        defaultUsers.primaryUser.userTypedId, transactionToConfirm.transactionId, rule.uid)
    dataHelper.createLastRuleApplied(
        defaultUsers.primaryUser.userTypedId, transactionToDoNothing.transactionId, rule.uid)
    val request =
        UpdateTransactionsRequest(
            transactions =
                setOf(transactionToCategorize, transactionToConfirm, transactionToDoNothing))

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
                defaultUsers.primaryUser.userTypedId, transactionToCategorize.transactionId))
        .isNull()
    assertThat(
            lastRuleAppliedRepository.findByUserIdAndTransactionId(
                defaultUsers.primaryUser.userTypedId, transactionToConfirm.transactionId))
        .isNull()
    assertThat(
            lastRuleAppliedRepository.findByUserIdAndTransactionId(
                defaultUsers.primaryUser.userTypedId, transactionToDoNothing.transactionId))
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
    dataHelper.createLastRuleApplied(
        defaultUsers.primaryUser.userTypedId, user1Transactions[0].uid, rule.uid)
    dataHelper.createLastRuleApplied(
        defaultUsers.primaryUser.userTypedId, user1Transactions[1].uid, rule.uid)
    dataHelper.createLastRuleApplied(
        defaultUsers.primaryUser.userTypedId, user1Transactions[2].uid, rule.uid)

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
    doUpdate(user1Transactions[0].uid, user1Categories[0].uid, false)
    // Confirm
    doUpdate(user1Transactions[1].uid, null, true)
    // Neither
    doUpdate(user1Transactions[2].uid, null, false)

    entityManager.flushAndClear()

    assertThat(
            lastRuleAppliedRepository.findByUserIdAndTransactionId(
                defaultUsers.primaryUser.userTypedId, user1Transactions[0].uid))
        .isNull()
    assertThat(
            lastRuleAppliedRepository.findByUserIdAndTransactionId(
                defaultUsers.primaryUser.userTypedId, user1Transactions[1].uid))
        .isNull()
    assertThat(
            lastRuleAppliedRepository.findByUserIdAndTransactionId(
                defaultUsers.primaryUser.userTypedId, user1Transactions[2].uid))
        .isNotNull
  }

  @Test
  fun `deleteTransactions - clears last rule applied`() {
    dataHelper.createLastRuleApplied(
        defaultUsers.primaryUser.userTypedId, user1Transactions[0].uid, rule.uid)
    val request = DeleteTransactionsRequest(ids = setOf(user1Transactions[0].uid))

    mockMvc
        .delete("/transactions") {
          secure = true
          header("Authorization", "Bearer $token")
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect { status { isNoContent() } }

    assertThat(
            lastRuleAppliedRepository.findByUserIdAndTransactionId(
                defaultUsers.primaryUser.userTypedId, user1Transactions[0].uid))
        .isNull()
  }

  @Test
  fun `markNotDuplicate - different user id`() {
    val txn1 = user2Transactions[0]
    transactionRepository.saveAndFlush(Transaction(txn1))
    transactionRepository.saveAndFlush(Transaction(txn1))
    entityManager.flushAndClear()

    mockMvc
        .put("/transactions/${txn1.uid}/notDuplicate") {
          secure = true
          header("Authorization", "Bearer $token")
        }
        .andExpect { status { isNoContent() } }

    entityManager.flushAndClear()

    val txn1Duplicates =
        transactionViewRepository.findAllDuplicates(
            txn1.uid, defaultUsers.secondaryUser.userTypedId, PageRequest.of(0, 25))
    assertThat(txn1Duplicates).hasSize(2)
  }

  @Test
  fun getTransactionDetails() {
    val txn = user1Transactions[0]
    val responseString =
        mockMvc
            .get("/transactions/${txn.uid}/details") {
              secure = true
              header("Authorization", "Bearer $token")
            }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString
    val response = objectMapper.readValue(responseString, TransactionDetailsResponse::class.java)
    val baseExpected =
        transactionViewRepository.findById(txn.uid).orElseThrow().let {
          TransactionDetailsResponse.from(it)
        }
    val expected =
        baseExpected.copy(
            created = baseExpected.created.withZoneSameInstant(ZoneId.of("UTC")),
            updated = baseExpected.updated.withZoneSameInstant(ZoneId.of("UTC")))
    assertEquals(expected, response)
  }

  @Test
  fun `getTransactionDetails - user does not have access`() {
    val txn = user2Transactions[0]
    mockMvc
        .get("/transactions/${txn.uid}/details") {
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
