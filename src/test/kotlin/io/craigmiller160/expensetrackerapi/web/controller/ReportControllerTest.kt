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
import io.craigmiller160.expensetrackerapi.testutils.toQueryString
import io.craigmiller160.expensetrackerapi.testutils.userTypedId
import io.craigmiller160.expensetrackerapi.web.types.report.ReportCategoryIdFilterType
import io.craigmiller160.expensetrackerapi.web.types.report.ReportCategoryResponse
import io.craigmiller160.expensetrackerapi.web.types.report.ReportMonthResponse
import io.craigmiller160.expensetrackerapi.web.types.report.ReportPageResponse
import io.craigmiller160.expensetrackerapi.web.types.report.ReportRequest
import jakarta.persistence.EntityManager
import java.math.BigDecimal
import java.time.LocalDate
import java.util.stream.Stream
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
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
  companion object {
    @JvmStatic
    fun reportRequestValidation(): Stream<ControllerValidationConfig<ReportRequest>> {
      val request =
          ReportRequest(
              pageNumber = 0,
              pageSize = 50,
              categoryIdType = ReportCategoryIdFilterType.INCLUDE,
              categoryIds = listOf())

      return Stream.of(
          ControllerValidationConfig(request, 200),
          ControllerValidationConfig(
              request.copy(pageNumber = -1), 400, "pageNumber: must be greater than or equal to 0"),
          ControllerValidationConfig(
              request.copy(pageSize = 150), 400, "pageSize: must be less than or equal to 100"),
          ControllerValidationConfig(
              request.copy(pageSize = -1), 400, "pageSize: must be greater than or equal to 0"))
    }
  }

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
                                    id = cat1.id,
                                    name = cat1.name,
                                    color = cat1.color,
                                    amount = txn3.amount,
                                    percent = txn3.amount / month2Total),
                                ReportCategoryResponse(
                                    id = cat2.id,
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
                                    id = cat1.id,
                                    name = cat1.name,
                                    color = cat1.color,
                                    amount = txn1.amount,
                                    percent = txn1.amount / month1Total),
                                ReportCategoryResponse(
                                    id = cat2.id,
                                    name = cat2.name,
                                    color = cat2.color,
                                    amount = txn2.amount,
                                    percent = txn2.amount / month1Total),
                                ReportCategoryResponse(
                                    id = cat4.id,
                                    name = cat4.name,
                                    color = cat4.color,
                                    amount = txn8.amount,
                                    percent = txn8.amount / month1Total),
                                ReportCategoryResponse(
                                    id = cat5.id,
                                    name = cat5.name,
                                    color = cat5.color,
                                    amount = txn7.amount,
                                    percent = txn7.amount / month1Total),
                                ReportCategoryResponse(
                                    id = CategoryConstants.UNKNOWN_CATEGORY.id,
                                    name = CategoryConstants.UNKNOWN_CATEGORY.name,
                                    color = CategoryConstants.UNKNOWN_CATEGORY.color,
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

  @Test
  fun getReports_includeUnknown() {
    val unknownCategoryId = CategoryConstants.UNKNOWN_CATEGORY.id
    val category = expectedResponse.reports[1].categories[4].copy(percent = BigDecimal("1.0"))
    val report =
        expectedResponse.reports[1].copy(total = category.amount, categories = listOf(category))
    val response = expectedResponse.copy(totalItems = 1, reports = listOf(report))
    mockMvc
        .get(
            "/reports?pageNumber=0&pageSize=100&categoryIdType=INCLUDE&categoryIds=$unknownCategoryId") {
              secure = true
              header("Authorization", "Bearer $token")
            }
        .andExpect {
          status { isOk() }
          content { json(objectMapper.writeValueAsString(response), true) }
        }
  }

  @Test
  fun getReports_includeCategoryAndUnknown() {
    val month2ReportTotal = expectedResponse.reports[0].categories[1].amount
    val month2Report =
        expectedResponse.reports[0].copy(
            total = month2ReportTotal,
            categories =
                listOf(expectedResponse.reports[0].categories[1].copy(percent = BigDecimal("1.0"))))

    val month1ReportTotal =
        expectedResponse.reports[1].categories[1].amount +
            expectedResponse.reports[1].categories[4].amount
    val month1Report =
        expectedResponse.reports[1].copy(
            total = month1ReportTotal,
            categories =
                listOf(
                    expectedResponse.reports[1]
                        .categories[1]
                        .copy(
                            percent =
                                expectedResponse.reports[1].categories[1].amount /
                                    month1ReportTotal),
                    expectedResponse.reports[1]
                        .categories[4]
                        .copy(
                            percent =
                                expectedResponse.reports[1].categories[4].amount /
                                    month1ReportTotal)))

    val response = expectedResponse.copy(reports = listOf(month2Report, month1Report))

    val unknownCategoryId = CategoryConstants.UNKNOWN_CATEGORY.id
    val categoryIds = listOf(unknownCategoryId, categories[1].id).joinToString(",")
    mockMvc
        .get("/reports?pageNumber=0&pageSize=100&categoryIdType=INCLUDE&categoryIds=$categoryIds") {
          secure = true
          header("Authorization", "Bearer $token")
        }
        .andExpect {
          status { isOk() }
          content { json(objectMapper.writeValueAsString(response), true) }
        }
  }

  @Test
  fun getReports_excludeUnknown() {
    val unknownCategoryId = CategoryConstants.UNKNOWN_CATEGORY.id
    val newMonth1Total =
        expectedResponse.reports[1].total - expectedResponse.reports[1].categories[4].amount
    val newMonth1Report =
        expectedResponse.reports[1].copy(
            total = newMonth1Total,
            categories =
                expectedResponse.reports[1].categories.slice(0..3).map { cat ->
                  cat.copy(percent = cat.amount / newMonth1Total)
                })

    val response =
        expectedResponse.copy(reports = listOf(expectedResponse.reports[0], newMonth1Report))
    mockMvc
        .get(
            "/reports?pageNumber=0&pageSize=100&categoryIdType=EXCLUDE&categoryIds=$unknownCategoryId") {
              secure = true
              header("Authorization", "Bearer $token")
            }
        .andExpect {
          status { isOk() }
          content { json(objectMapper.writeValueAsString(response), true) }
        }
  }

  @Test
  fun getReports_excludeCategoryAndUnknown() {
    val month2Total = expectedResponse.reports[0].categories[0].amount
    val month2Report =
        expectedResponse.reports[0].copy(
            total = month2Total,
            categories =
                listOf(expectedResponse.reports[0].categories[0].copy(percent = BigDecimal("1.0"))))

    val month1Total =
        expectedResponse.reports[1].total -
            expectedResponse.reports[1].categories[1].amount -
            expectedResponse.reports[1].categories[4].amount
    val month1Report =
        expectedResponse.reports[1].copy(
            total = month1Total,
            categories =
                listOf(
                    expectedResponse.reports[1]
                        .categories[0]
                        .copy(
                            percent =
                                expectedResponse.reports[1].categories[0].amount / month1Total),
                    expectedResponse.reports[1]
                        .categories[2]
                        .copy(
                            percent =
                                expectedResponse.reports[1].categories[2].amount / month1Total),
                    expectedResponse.reports[1]
                        .categories[3]
                        .copy(
                            percent =
                                expectedResponse.reports[1].categories[3].amount / month1Total)))

    val response = expectedResponse.copy(reports = listOf(month2Report, month1Report))

    val unknownCategoryId = CategoryConstants.UNKNOWN_CATEGORY.id
    val categoryIds = listOf(unknownCategoryId, categories[1].id).joinToString(",")
    mockMvc
        .get("/reports?pageNumber=0&pageSize=100&categoryIdType=EXCLUDE&categoryIds=$categoryIds") {
          secure = true
          header("Authorization", "Bearer $token")
        }
        .andExpect {
          status { isOk() }
          content { json(objectMapper.writeValueAsString(response), true) }
        }
  }

  @ParameterizedTest
  @MethodSource("reportRequestValidation")
  fun `validate report request`(config: ControllerValidationConfig<ReportRequest>) {
    ControllerValidationSupport.validate(config) {
      mockMvc.get("/reports?${config.request.toQueryString(objectMapper)}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
    }
  }
}
