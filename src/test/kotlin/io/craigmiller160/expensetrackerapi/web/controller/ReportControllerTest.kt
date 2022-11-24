package io.craigmiller160.expensetrackerapi.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.expensetrackerapi.data.repository.ReportRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.extension.flushAndClear
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testcore.OAuth2Extension
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import io.craigmiller160.expensetrackerapi.web.types.report.ReportCategoryResponse
import io.craigmiller160.expensetrackerapi.web.types.report.ReportMonthResponse
import io.craigmiller160.expensetrackerapi.web.types.report.ReportPageResponse
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
  private val entityManager: EntityManager
) {
  private lateinit var token: String

  private lateinit var expectedResponse: ReportPageResponse

  @BeforeEach
  fun setup() {
    token = OAuth2Extension.createJwt()
    val cat1 = dataHelper.createCategory(1L, "Entertainment")
    val cat2 = dataHelper.createCategory(1L, "Groceries")
    val cat3 = dataHelper.createCategory(2L, "Food")
    val cat4 = dataHelper.createCategory(1L, "Restaurants")
    val cat5 = dataHelper.createCategory(1L, "Travel")

    val month1 = LocalDate.of(2022, 1, 1)
    val month2 = LocalDate.of(2022, 2, 1)

    val txn1 =
      dataHelper.createTransaction(1L, cat1.id).let {
        transactionRepository.save(it.apply { expenseDate = month1.plusDays(1) })
      }
    val txn2 =
      dataHelper.createTransaction(1L, cat2.id).let {
        transactionRepository.save(it.apply { expenseDate = month1.plusDays(2) })
      }
    val txn3 =
      dataHelper.createTransaction(1L, cat1.id).let {
        transactionRepository.save(it.apply { expenseDate = month2.plusDays(3) })
      }
    val txn4 =
      dataHelper.createTransaction(1L, cat2.id).let {
        transactionRepository.save(it.apply { expenseDate = month2.plusDays(4) })
      }
    dataHelper.createTransaction(2L, cat3.id).let {
      transactionRepository.save(it.apply { expenseDate = month1.plusDays(5) })
    }
    val txn6 =
      dataHelper.createTransaction(1L).let {
        transactionRepository.save(it.apply { expenseDate = month1.plusDays(6) })
      }
    val txn7 =
      dataHelper.createTransaction(1L, cat5.id).let {
        transactionRepository.save(it.apply { expenseDate = month1.plusDays(7) })
      }
    val txn8 =
      dataHelper.createTransaction(1L, cat4.id).let {
        transactionRepository.save(it.apply { expenseDate = month1.plusDays(8) })
      }

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
                    name = ReportRepository.UNKNOWN_CATEGORY_NAME,
                    color = ReportRepository.UNKNOWN_CATEGORY_COLOR,
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
}
