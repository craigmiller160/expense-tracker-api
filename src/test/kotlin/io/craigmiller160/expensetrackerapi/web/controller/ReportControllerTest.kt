package io.craigmiller160.expensetrackerapi.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import java.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc

@ExpenseTrackerIntegrationTest
class ReportControllerTest
@Autowired
constructor(
  private val mockMvc: MockMvc,
  private val objectMapper: ObjectMapper,
  private val dataHelper: DataHelper,
  private val transactionRepository: TransactionRepository
) {
  @BeforeEach
  fun setup() {
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
  }
  @Test
  fun getReports() {
    TODO()
  }
}
