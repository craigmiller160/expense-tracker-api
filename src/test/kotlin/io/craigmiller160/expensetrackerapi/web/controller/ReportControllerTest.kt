package io.craigmiller160.expensetrackerapi.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testcore.OAuth2Extension
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import io.craigmiller160.expensetrackerapi.web.types.report.ReportCategoryResponse
import io.craigmiller160.expensetrackerapi.web.types.report.ReportMonthResponse
import io.craigmiller160.expensetrackerapi.web.types.report.ReportPageResponse
import java.time.LocalDate
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
) {
  private lateinit var token: String

  private lateinit var expectedResponse: ReportPageResponse

  @BeforeEach
  fun setup() {
    token = OAuth2Extension.createJwt()
    val cat1 = dataHelper.createCategory(1L, "Entertainment")
    val cat2 = dataHelper.createCategory(1L, "Groceries")
    val cat3 = dataHelper.createCategory(2L, "Food")

    val month1 = LocalDate.of(2022, 1, 1)
    val month2 = LocalDate.of(2022, 2, 1)

    val txn1 =
      dataHelper.createTransaction(1L, cat1.id).let {
        transactionRepository.save(it.copy(expenseDate = month1))
      }
    val txn2 =
      dataHelper.createTransaction(1L, cat2.id).let {
        transactionRepository.save(it.copy(expenseDate = month1))
      }
    val txn3 =
      dataHelper.createTransaction(1L, cat1.id).let {
        transactionRepository.save(it.copy(expenseDate = month2))
      }
    val txn4 =
      dataHelper.createTransaction(1L, cat2.id).let {
        transactionRepository.save(it.copy(expenseDate = month2))
      }
    val txn5 =
      dataHelper.createTransaction(2L, cat3.id).let {
        transactionRepository.save(it.copy(expenseDate = month1))
      }

    val month1Total = txn1.amount + txn2.amount
    val month2Total = txn3.amount + txn4.amount
    expectedResponse =
      ReportPageResponse(
        pageNumber = 0,
        totalItems = 2,
        reports =
          listOf(
            ReportMonthResponse(
              date = month1,
              total = month1Total,
              categories =
                listOf(
                  ReportCategoryResponse(
                    name = cat1.name, amount = txn1.amount, percent = txn1.amount / month1Total),
                  ReportCategoryResponse(
                    name = cat2.name, amount = txn2.amount, percent = txn2.amount / month1Total))),
            ReportMonthResponse(
              date = month2,
              total = month2Total,
              categories =
                listOf(
                  ReportCategoryResponse(
                    name = cat1.name, amount = txn3.amount, percent = txn3.amount / month2Total),
                  ReportCategoryResponse(
                    name = cat2.name, amount = txn4.amount, percent = txn4.amount / month2Total)))))
  }

  @Test
  fun getReports() {
    mockMvc
      .get("/reports?pageNumber=0&pageSize=10") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        // TODO I'm concerned the order is wrong here
        content { json(objectMapper.writeValueAsString(expectedResponse)) }
      }
  }

  @Test
  fun getReports_oneMonth() {
    val filteredResponse = expectedResponse.copy(reports = listOf(expectedResponse.reports[1]))
    mockMvc
      .get("/reports?pageNumber=0&pageSize=1") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(filteredResponse)) }
      }
  }
}
