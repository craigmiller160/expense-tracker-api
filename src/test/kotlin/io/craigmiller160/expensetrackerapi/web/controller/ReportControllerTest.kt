package io.craigmiller160.expensetrackerapi.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.expensetrackerapi.data.constants.CategoryConstants
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.extension.flushAndClear
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import io.craigmiller160.expensetrackerapi.testutils.DefaultUsers
import io.craigmiller160.expensetrackerapi.testutils.userTypedId
import io.craigmiller160.expensetrackerapi.web.types.report.ReportCategoryResponse
import io.craigmiller160.expensetrackerapi.web.types.report.ReportMonthResponse
import io.craigmiller160.expensetrackerapi.web.types.report.ReportPageResponse
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@ExpenseTrackerIntegrationTest
class ReportControllerTest
@Autowired
constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val dataHelper: DataHelper,
    private val transactionRepository: TransactionRepository,
    private val entityManager: EntityManager,
    private val defaultUsers: DefaultUsers
) {
  private lateinit var token: String

  private lateinit var expectedResponse: ReportPageResponse
  private lateinit var categories: List<Category>
  private lateinit var transactions: List<Transaction>

  @BeforeEach
  fun setup() {
    token = defaultUsers.primaryUser.token
    val cat1 = dataHelper.createCategory(defaultUsers.primaryUser.userTypedId, "Entertainment")
    val cat2 = dataHelper.createCategory(defaultUsers.primaryUser.userTypedId, "Groceries")
    val cat3 = dataHelper.createCategory(defaultUsers.secondaryUser.userTypedId, "Food")
    val cat4 = dataHelper.createCategory(defaultUsers.primaryUser.userTypedId, "Restaurants")
    val cat5 = dataHelper.createCategory(defaultUsers.primaryUser.userTypedId, "Travel")
    categories = listOf(cat1, cat2, cat3, cat4, cat5)

    val month1 = LocalDate.of(2022, 1, 1)
    val month2 = LocalDate.of(2022, 2, 1)

    val txn1 =
        dataHelper.createTransaction(defaultUsers.primaryUser.userTypedId, cat1.uid).let {
          transactionRepository.save(it.apply { expenseDate = month1.plusDays(1) })
        }
    val txn2 =
        dataHelper.createTransaction(defaultUsers.primaryUser.userTypedId, cat2.uid).let {
          transactionRepository.save(it.apply { expenseDate = month1.plusDays(2) })
        }
    val txn3 =
        dataHelper.createTransaction(defaultUsers.primaryUser.userTypedId, cat1.uid).let {
          transactionRepository.save(it.apply { expenseDate = month2.plusDays(3) })
        }
    val txn4 =
        dataHelper.createTransaction(defaultUsers.primaryUser.userTypedId, cat2.uid).let {
          transactionRepository.save(it.apply { expenseDate = month2.plusDays(4) })
        }
    val txn5 =
        dataHelper.createTransaction(defaultUsers.secondaryUser.userTypedId, cat3.uid).let {
          transactionRepository.save(it.apply { expenseDate = month1.plusDays(5) })
        }
    val txn6 =
        dataHelper.createTransaction(defaultUsers.primaryUser.userTypedId).let {
          transactionRepository.save(it.apply { expenseDate = month1.plusDays(6) })
        }
    val txn7 =
        dataHelper.createTransaction(defaultUsers.primaryUser.userTypedId, cat5.uid).let {
          transactionRepository.save(it.apply { expenseDate = month1.plusDays(7) })
        }
    val txn8 =
        dataHelper.createTransaction(defaultUsers.primaryUser.userTypedId, cat4.uid).let {
          transactionRepository.save(it.apply { expenseDate = month1.plusDays(8) })
        }
    transactions = listOf(txn1, txn2, txn3, txn4, txn5, txn6, txn7, txn8)

    entityManager.flushAndClear()

    val month1Total = txn1.amount + txn2.amount + txn6.amount + txn7.amount + txn8.amount
    val month2Total = txn3.amount + txn4.amount
    expectedResponse =
        ReportPageResponse(
            pageNumber = 0,
            totalItems = 2,
            reports =
                listOf(
                    ReportMonthResponse(
                        date = month2,
                        total = month2Total,
                        categories =
                            listOf(
                                ReportCategoryResponse(
                                    name = cat1.name,
                                    color = cat1.color,
                                    amount = txn3.amount,
                                    percent = txn3.amount / month2Total),
                                ReportCategoryResponse(
                                    name = cat2.name,
                                    color = cat2.color,
                                    amount = txn4.amount,
                                    percent = txn4.amount / month2Total))),
                    ReportMonthResponse(
                        date = month1,
                        total = month1Total,
                        categories =
                            listOf(
                                ReportCategoryResponse(
                                    name = cat1.name,
                                    color = cat1.color,
                                    amount = txn1.amount,
                                    percent = txn1.amount / month1Total),
                                ReportCategoryResponse(
                                    name = cat2.name,
                                    color = cat2.color,
                                    amount = txn2.amount,
                                    percent = txn2.amount / month1Total),
                                ReportCategoryResponse(
                                    name = cat4.name,
                                    color = cat4.color,
                                    amount = txn8.amount,
                                    percent = txn8.amount / month1Total),
                                ReportCategoryResponse(
                                    name = cat5.name,
                                    color = cat5.color,
                                    amount = txn7.amount,
                                    percent = txn7.amount / month1Total),
                                ReportCategoryResponse(
                                    name = CategoryConstants.UNKNOWN_CATEGORY_NAME,
                                    color = CategoryConstants.UNKNOWN_CATEGORY_COLOR,
                                    amount = txn6.amount,
                                    percent = txn6.amount / month1Total)))))
  }

  @Test
  fun getReports() {
    mockMvc
        .get("/reports?pageNumber=0&pageSize=10") {
          secure = true
          header("Authorization", "Bearer $token")
        }
        .andDo { print() }
        .andExpect {
          status { isOk() }
          content { json(objectMapper.writeValueAsString(expectedResponse), true) }
        }
  }

  @Test
  fun getReports_oneMonth() {
    val filteredResponse = expectedResponse.copy(reports = listOf(expectedResponse.reports[0]))
    mockMvc
        .get("/reports?pageNumber=0&pageSize=1") {
          secure = true
          header("Authorization", "Bearer $token")
        }
        .andExpect {
          status { isOk() }
          content { json(objectMapper.writeValueAsString(filteredResponse), true) }
        }
  }

  @Test
  fun getReports_noTransactions() {
    transactionRepository.deleteAll()
    entityManager.flushAndClear()

    val response = ReportPageResponse(reports = listOf(), pageNumber = 0, totalItems = 0)

    mockMvc
        .get("/reports?pageNumber=0&pageSize=1") {
          secure = true
          header("Authorization", "Bearer $token")
        }
        .andExpect {
          status { isOk() }
          content { json(objectMapper.writeValueAsString(response), true) }
        }
  }

  @Test
  fun getReports_excludeCategory_noRecordsForMonth() {
    val response =
        expectedResponse.copy(
            totalItems = 1,
            reports =
                expectedResponse.reports
                    .filterIndexed { index, _ -> index == 1 }
                    .map { report ->
                      val newTotal = report.total - transactions[1].amount - transactions[0].amount
                      report.copy(
                          total = newTotal,
                          categories =
                              report.categories
                                  .filter {
                                    it.name != categories[1].name && it.name != categories[0].name
                                  }
                                  .map { it.copy(percent = it.amount / newTotal) })
                    })
    mockMvc
        .get(
            "/reports?pageNumber=0&pageSize=100&categoryIds=${categories[0].id},${categories[1].id}") {
              secure = true
              header("Authorization", "Bearer $token")
            }
        .andExpect {
          status { isOk() }
          content { json(objectMapper.writeValueAsString(response), true) }
        }
  }

  @Test
  fun getReports_excludeCategory_explicitType() {
    val response =
        expectedResponse.copy(
            reports =
                expectedResponse.reports.mapIndexed { index, report ->
                  val newTotal =
                      report.total -
                          when (index) {
                            0 -> transactions[3].amount
                            else -> transactions[1].amount
                          }
                  report.copy(
                      categories =
                          report.categories
                              .filter { it.name != categories[1].name }
                              .map { it.copy(percent = it.amount / newTotal) },
                      total = newTotal)
                })

    mockMvc
        .get(
            "/reports?pageNumber=0&pageSize=100&categoryIdType=EXCLUDE&categoryIds=${categories[1].id}") {
              secure = true
              header("Authorization", "Bearer $token")
            }
        .andExpect {
          status { isOk() }
          content { json(objectMapper.writeValueAsString(response), true) }
        }
  }

  @Test
  fun getReports_excludeCategory() {
    val response =
        expectedResponse.copy(
            reports =
                expectedResponse.reports.mapIndexed { index, report ->
                  val newTotal =
                      report.total -
                          when (index) {
                            0 -> transactions[3].amount
                            else -> transactions[1].amount
                          }
                  report.copy(
                      categories =
                          report.categories
                              .filter { it.name != categories[1].name }
                              .map { it.copy(percent = it.amount / newTotal) },
                      total = newTotal)
                })

    mockMvc
        .get("/reports?pageNumber=0&pageSize=100&categoryIds=${categories[1].id}") {
          secure = true
          header("Authorization", "Bearer $token")
        }
        .andExpect {
          status { isOk() }
          content { json(objectMapper.writeValueAsString(response), true) }
        }
  }

  @Test
  fun getReports_includeCategory() {
    val response =
        expectedResponse.copy(
            reports =
                expectedResponse.reports.mapIndexed { index, report ->
                  val newTotal = if (index == 0) transactions[3].amount else transactions[1].amount
                  report.copy(
                      categories =
                          report.categories
                              .filter { it.name == categories[1].name }
                              .map { it.copy(percent = BigDecimal("1.0")) },
                      total = newTotal)
                })

    mockMvc
        .get(
            "/reports?pageNumber=0&pageSize=100&categoryIdType=INCLUDE&categoryIds=${categories[1].id}") {
              secure = true
              header("Authorization", "Bearer $token")
            }
        .andExpect {
          status { isOk() }
          content { json(objectMapper.writeValueAsString(response), true) }
        }
  }
}
