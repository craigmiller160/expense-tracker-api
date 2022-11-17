package io.craigmiller160.expensetrackerapi.service

import arrow.core.getOrHandle
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import java.math.BigDecimal
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ExpenseTrackerIntegrationTest
class AutoApplyCategoriesToTransactionsServiceTest
@Autowired
constructor(
  private val dataHelper: DataHelper,
  private val autoCategorizeRuleRepository: AutoCategorizeRuleRepository,
  private val transactionRepository: TransactionRepository,
  private val applyCategoriesToTransactionsService: AutoApplyCategoriesToTransactionsService
) {
  // TODO how to handle case-insensitivity? Do I care at this time?

  private var ruleCounter = 0

  /*
   * 4) Start Date
   * 5) End Date
   */

  private lateinit var transactions: List<Transaction>
  private lateinit var categories: List<Category>

  @BeforeEach
  fun setup() {
    ruleCounter = 0
    val cat1 = dataHelper.createCategory(1L, "Entertainment")
    val cat2 = dataHelper.createCategory(1L, "Food")
    val cat3 = dataHelper.createCategory(1L, "Bills")

    categories = listOf(cat1, cat2, cat3)

    val txn1 = createTransaction("Target", LocalDate.of(2022, 1, 1), BigDecimal("10"))
    val txn2 = createTransaction("Target", LocalDate.of(2022, 1, 1), BigDecimal("100"))
    val txn3 = createTransaction("AMC Theaters", LocalDate.of(2022, 3, 10), BigDecimal("22"))
    val txn4 = createTransaction("AMC Theaters", LocalDate.of(2022, 6, 1), BigDecimal("10"))
    val txn5 = createTransaction("AMC Theaters 2", LocalDate.of(2022, 3, 10), BigDecimal("22"))
    val txn6 = createTransaction("Target", LocalDate.of(2022, 1, 1), BigDecimal("20"))

    transactions = listOf(txn1, txn2, txn3, txn4, txn5, txn6)

    val rule1 = createRule(categoryId = cat1.id, regex = "Target", minAmount = BigDecimal("90"))
    val rule2 = createRule(categoryId = cat2.id, regex = "Target", maxAmount = BigDecimal("15"))
    val rule3 = createRule(categoryId = cat3.id, regex = "Target")
  }

  private fun createTransaction(
    description: String,
    expenseDate: LocalDate,
    amount: BigDecimal
  ): Transaction =
    transactionRepository.save(
      Transaction(
        userId = 1L, expenseDate = expenseDate, description = description, amount = amount))

  private fun createRule(
    categoryId: TypedId<CategoryId>,
    regex: String,
    startDate: LocalDate? = null,
    endDate: LocalDate? = null,
    minAmount: BigDecimal? = null,
    maxAmount: BigDecimal? = null
  ): AutoCategorizeRule =
    autoCategorizeRuleRepository.save(
      AutoCategorizeRule(
        userId = 1L,
        categoryId = categoryId,
        ordinal = ++ruleCounter,
        regex = regex,
        startDate = startDate,
        endDate = endDate,
        minAmount = minAmount,
        maxAmount = maxAmount))

  @Test
  fun applyCategoriesToTransactions() {
    val result =
      applyCategoriesToTransactionsService
        .applyCategoriesToTransactions(1L, transactions)
        .getOrHandle { throw it }
    assertThat(result[1]).hasFieldOrPropertyWithValue("categoryId", categories[0].id)
    assertThat(result[0]).hasFieldOrPropertyWithValue("categoryId", categories[1].id)
    assertThat(result[5]).hasFieldOrPropertyWithValue("categoryId", categories[2].id)
  }
}
