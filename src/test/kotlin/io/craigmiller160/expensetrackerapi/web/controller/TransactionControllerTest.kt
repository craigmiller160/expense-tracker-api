package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.BaseIntegrationTest
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionViewRepository
import io.craigmiller160.expensetrackerapi.web.types.CategorizeTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.ConfirmTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.CountAndOldest
import io.craigmiller160.expensetrackerapi.web.types.CreateTransactionRequest
import io.craigmiller160.expensetrackerapi.web.types.DeleteTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.NeedsAttentionResponse
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsRequest
import io.craigmiller160.expensetrackerapi.web.types.SearchTransactionsResponse
import io.craigmiller160.expensetrackerapi.web.types.SortDirection
import io.craigmiller160.expensetrackerapi.web.types.TransactionAndCategory
import io.craigmiller160.expensetrackerapi.web.types.TransactionResponse
import io.craigmiller160.expensetrackerapi.web.types.TransactionSortKey
import io.craigmiller160.expensetrackerapi.web.types.TransactionToConfirm
import io.craigmiller160.expensetrackerapi.web.types.TransactionToUpdate
import io.craigmiller160.expensetrackerapi.web.types.UpdateTransactionDetailsRequest
import io.craigmiller160.expensetrackerapi.web.types.UpdateTransactionsRequest
import java.math.BigDecimal
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

class TransactionControllerTest : BaseIntegrationTest() {
  @Autowired private lateinit var transactionRepository: TransactionRepository

  private lateinit var user1Categories: List<Category>
  private lateinit var user1CategoriesMap: Map<TypedId<CategoryId>, Category>
  private lateinit var user1Transactions: List<Transaction>
  private lateinit var user2Transactions: List<Transaction>
  @Autowired private lateinit var transactionViewRepository: TransactionViewRepository

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
            transaction.copy(categoryId = user1Categories[index % 3].id, confirmed = true))
        } else {
          transaction
        }
      }
    user2Transactions = user2Txns
    user1CategoriesMap = user1Categories.associateBy { it.id }
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
      SearchTransactionsResponse(
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
        content { json(objectMapper.writeValueAsString(response)) }
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
      SearchTransactionsResponse(
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
        content { json(objectMapper.writeValueAsString(response)) }
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
      SearchTransactionsResponse(
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
        content { json(objectMapper.writeValueAsString(response)) }
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
      SearchTransactionsResponse(
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
        content { json(objectMapper.writeValueAsString(response)) }
      }
  }

  @Test
  fun `search - possible refunds`() {
    val txn1 =
      transactionRepository.saveAndFlush(
        user1Transactions[0].copy(amount = user1Transactions[0].amount * BigDecimal("-1")))
    val txn2 =
      transactionRepository.saveAndFlush(
        user1Transactions[2].copy(amount = user1Transactions[2].amount * BigDecimal("-1")))

    val request =
      SearchTransactionsRequest(
        isPossibleRefund = true,
        pageNumber = 0,
        pageSize = 100,
        sortKey = TransactionSortKey.EXPENSE_DATE,
        sortDirection = SortDirection.ASC)

    val response =
      SearchTransactionsResponse(
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
        content { json(objectMapper.writeValueAsString(response)) }
      }
  }

  @Test
  fun `search - only duplicates`() {
    val txn1 = user1Transactions[0]
    val txn2 = transactionRepository.saveAndFlush(txn1.copy(id = TypedId()))
    entityManager.flush()
    entityManager.clear()

    val request =
      SearchTransactionsRequest(
        isDuplicate = true,
        pageNumber = 0,
        pageSize = 100,
        sortKey = TransactionSortKey.EXPENSE_DATE,
        sortDirection = SortDirection.ASC)

    val response =
      SearchTransactionsResponse(
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
        content { json(objectMapper.writeValueAsString(response)) }
      }
  }

  @Test
  fun `search - invalid category options`() {
    val request =
      SearchTransactionsRequest(
        isCategorized = false,
        categoryIds = setOf(user1Categories[0].id),
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
    val txn2 = transactionRepository.saveAndFlush(txn1.copy(id = TypedId()))
    val request =
      SearchTransactionsRequest(
        isDuplicate = false,
        pageNumber = 0,
        pageSize = 100,
        sortKey = TransactionSortKey.EXPENSE_DATE,
        sortDirection = SortDirection.ASC)

    val nonDuplicateIds = user1Transactions.subList(1, user1Transactions.size).map { it.id }
    val nonDuplicateTransactions = transactionViewRepository.findAllByIdIn(nonDuplicateIds)

    val response =
      SearchTransactionsResponse(
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
        content { json(objectMapper.writeValueAsString(response)) }
      }
  }

  @Test
  fun `search - confirmed transactions only`() {
    user1Transactions =
      user1Transactions.map { txn ->
        transactionRepository.saveAndFlush(txn.copy(confirmed = false))
      }
    val txn1 = transactionRepository.saveAndFlush(user1Transactions.first().copy(confirmed = true))
    val txn2 = transactionRepository.saveAndFlush(user1Transactions[1].copy(confirmed = true))
    transactionRepository.saveAndFlush(user2Transactions.first().copy(confirmed = true))
    val request =
      SearchTransactionsRequest(
        isConfirmed = true,
        pageNumber = 0,
        pageSize = 100,
        sortKey = TransactionSortKey.EXPENSE_DATE,
        sortDirection = SortDirection.ASC)

    val response =
      SearchTransactionsResponse(
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
        content { json(objectMapper.writeValueAsString(response)) }
      }
  }

  @Test
  fun `get data on what records need attention, when all types need attention`() {
    //    val oldestUnconfirmed =
    //      transactionRepository.saveAndFlush(user1Transactions[0].copy(confirmed = false))
    //    val oldestDuplicate =
    //      transactionRepository.saveAndFlush(user1Transactions[2].copy(duplicate = true))
    //    val oldestPossibleRefund =
    //      transactionRepository.saveAndFlush(
    //        user1Transactions[3].copy(amount = user1Transactions[3].amount * BigDecimal("-1")))
    //    val response =
    //      NeedsAttentionResponse(
    //        unconfirmed = CountAndOldest(count = 4, oldest = oldestUnconfirmed.expenseDate),
    //        uncategorized = CountAndOldest(count = 3, oldest = user1Transactions[1].expenseDate),
    //        duplicate = CountAndOldest(count = 1, oldest = oldestDuplicate.expenseDate),
    //        possibleRefund = CountAndOldest(count = 1, oldest = oldestPossibleRefund.expenseDate))
    //    mockMvc
    //      .get("/transactions/needs-attention") {
    //        secure = true
    //        header("Authorization", "Bearer $token")
    //      }
    //      .andExpect {
    //        status { isOk() }
    //        content { json(objectMapper.writeValueAsString(response)) }
    //      }
    TODO()
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
      .get("/transactions/needs-attention") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(response)) }
      }
  }

  @Test
  fun `search - unconfirmed transactions only`() {
    user1Transactions =
      user1Transactions.map { txn ->
        transactionRepository.saveAndFlush(txn.copy(confirmed = false))
      }
    transactionRepository.saveAndFlush(user1Transactions.first().copy(confirmed = true))
    transactionRepository.saveAndFlush(user1Transactions[1].copy(confirmed = true))
    val request =
      SearchTransactionsRequest(
        isConfirmed = false,
        pageNumber = 0,
        pageSize = 100,
        sortKey = TransactionSortKey.EXPENSE_DATE,
        sortDirection = SortDirection.ASC)

    val response =
      SearchTransactionsResponse(
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
        .filter { it.expenseDate.isAfter(LocalDate.now().minusDays(3)) }

    val request =
      SearchTransactionsRequest(
        startDate = LocalDate.now().minusDays(2),
        endDate = LocalDate.now(),
        pageNumber = 0,
        pageSize = 100,
        sortKey = TransactionSortKey.EXPENSE_DATE,
        sortDirection = SortDirection.ASC)

    val response =
      SearchTransactionsResponse(
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
        content { json(objectMapper.writeValueAsString(response)) }
      }
  }

  @Test
  fun `search - categories`() {
    val user2Cat = dataHelper.createCategory(2L, "User2 Cat")
    transactionRepository.saveAndFlush(user2Transactions.first().copy(categoryId = user2Cat.id))

    val categories = setOf(user1Categories.first().id, user2Cat.id)

    val response =
      SearchTransactionsResponse(
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
        content { json(objectMapper.writeValueAsString(response)) }
      }
  }

  @Test
  fun deleteTransactions() {
    val request =
      DeleteTransactionsRequest(
        ids = setOf(user1Transactions.first().id, user2Transactions.first().id))

    mockMvc
      .delete("/transactions") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isNoContent() } }

    entityManager.flush()

    assertThat(transactionRepository.findById(user1Transactions.first().id)).isEmpty
    assertThat(transactionRepository.findById(user2Transactions.first().id)).isPresent
  }

  @Test
  fun categorizeTransactions_removeCategory() {
    assertThat(user1Transactions[0].categoryId).isNotNull
    val request =
      CategorizeTransactionsRequest(
        transactionsAndCategories = setOf(TransactionAndCategory(user1Transactions[0].id, null)))

    mockMvc
      .put("/transactions/categorize") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isNoContent() } }

    entityManager.flush()

    assertThat(transactionRepository.findById(user1Transactions[0].id))
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
            TransactionToConfirm(transactionId = user1Transactions[0].id, confirmed = true),
            TransactionToConfirm(transactionId = user2Transactions[0].id, confirmed = true)))

    mockMvc
      .put("/transactions/confirm") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isNoContent() } }

    assertThat(transactionRepository.findById(user1Transactions[0].id))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("confirmed", true)
    assertThat(transactionRepository.findById(user2Transactions[0].id))
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
    assertThat(categorizedTransaction.categoryId).isNotNull.isNotEqualTo(user1Categories.first().id)

    val request =
      CategorizeTransactionsRequest(
        transactionsAndCategories =
          setOf(
            TransactionAndCategory(uncategorizedTransaction.id, user1Categories.first().id),
            TransactionAndCategory(categorizedTransaction.id, user1Categories.first().id),
            TransactionAndCategory(user2Transactions.first().id, user1Categories.first().id),
            TransactionAndCategory(user1Transactions[2].id, user2Category.id)))

    mockMvc
      .put("/transactions/categorize") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isNoContent() } }

    entityManager.flush()

    assertThat(transactionRepository.findById(uncategorizedTransaction.id))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", user1Categories.first().id)
      .hasFieldOrPropertyWithValue("confirmed", false)
    assertThat(transactionRepository.findById(categorizedTransaction.id))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", user1Categories.first().id)
      .hasFieldOrPropertyWithValue("confirmed", true)
    assertThat(transactionRepository.findById(user2Transactions.first().id))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", null)
      .hasFieldOrPropertyWithValue("confirmed", false)
    assertThat(transactionRepository.findById(user1Transactions[2].id))
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
        categoryId = user1Categories[0].id)

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
        categoryId = user2Category.id)

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
    val transactionId = user1Transactions[0].id
    val request =
      UpdateTransactionDetailsRequest(
        transactionId = transactionId,
        confirmed = true,
        expenseDate = LocalDate.of(1990, 1, 1),
        description = "New Description",
        amount = BigDecimal("-112.57"),
        categoryId = user1Categories[0].id)

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
    val transactionId = user1Transactions[0].id
    val request =
      UpdateTransactionDetailsRequest(
        transactionId = transactionId,
        confirmed = true,
        expenseDate = LocalDate.of(1990, 1, 1),
        description = "New Description",
        amount = BigDecimal("-112.57"),
        categoryId = user2Category.id)

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
              transactionId = uncategorizedTransaction.id,
              categoryId = user1Categories.first().id,
              confirmed = false),
            TransactionToUpdate(
              transactionId = categorizedTransaction.id,
              categoryId = user1Categories.first().id,
              confirmed = false),
            TransactionToUpdate(
              transactionId = user2Transactions.first().id,
              categoryId = user1Categories.first().id,
              confirmed = false),
            TransactionToUpdate(
              transactionId = user1Transactions[2].id,
              categoryId = user2Category.id,
              confirmed = false),
            TransactionToUpdate(transactionId = user1Transactions[6].id, confirmed = true),
            TransactionToUpdate(transactionId = user2Transactions[0].id, confirmed = true)))

    mockMvc
      .put("/transactions") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isNoContent() } }

    assertThat(transactionRepository.findById(uncategorizedTransaction.id))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", user1Categories.first().id)
      .hasFieldOrPropertyWithValue("confirmed", false)
    assertThat(transactionRepository.findById(categorizedTransaction.id))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", user1Categories.first().id)
      .hasFieldOrPropertyWithValue("confirmed", false)
    assertThat(transactionRepository.findById(user2Transactions.first().id))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", null)
      .hasFieldOrPropertyWithValue("confirmed", false)
    assertThat(transactionRepository.findById(user1Transactions[2].id))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", null)
      .hasFieldOrPropertyWithValue("confirmed", false)

    assertThat(transactionRepository.findById(user1Transactions[6].id))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("confirmed", true)
  }

  @Test
  fun getAllPossibleDuplicates() {
    TODO()
  }

  @Test
  fun `createTransaction is duplicate`() {
    TODO()
  }

  @Test
  fun `updateTransactionDetails is duplicate`() {
    TODO()
  }
}
