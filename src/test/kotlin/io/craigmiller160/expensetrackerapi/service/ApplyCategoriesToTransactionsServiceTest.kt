package io.craigmiller160.expensetrackerapi.service

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepository
import io.craigmiller160.expensetrackerapi.data.repository.LastRuleAppliedRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import io.kotest.assertions.arrow.core.shouldBeRight
import java.math.BigDecimal
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ExpenseTrackerIntegrationTest
class ApplyCategoriesToTransactionsServiceTest
@Autowired
constructor(
  private val dataHelper: DataHelper,
  private val autoCategorizeRuleRepository: AutoCategorizeRuleRepository,
  private val transactionRepository: TransactionRepository,
  private val applyCategoriesToTransactionsService: ApplyCategoriesToTransactionsService,
  private val lastRuleAppliedRepository: LastRuleAppliedRepository
) {

  private var ruleCounter = 0

  private lateinit var transactions: List<Transaction>
  private lateinit var categories: List<Category>

  @BeforeEach
  fun setup() {
    ruleCounter = 0
    val cat1 = dataHelper.createCategory(1L, "Entertainment")
    val cat2 = dataHelper.createCategory(1L, "Food")
    val cat3 = dataHelper.createCategory(1L, "Bills")
    val cat4 = dataHelper.createCategory(1L, "Other")
    val cat5 = dataHelper.createCategory(1L, "Something")
    val cat6 = dataHelper.createCategory(1L, "Foo")
    val cat7 = dataHelper.createCategory(1L, "To Somewhere")

    categories = listOf(cat1, cat2, cat3, cat4, cat5, cat6, cat7)

    val txn1 = createTransaction("Target", LocalDate.of(2022, 1, 1), BigDecimal("10"))
    val txn2 = createTransaction("Target", LocalDate.of(2022, 1, 1), BigDecimal("100"))
    val txn3 = createTransaction("AMC Theaters", LocalDate.of(2022, 3, 10), BigDecimal("22"))
    val txn4 = createTransaction("AMC Theaters", LocalDate.of(2022, 6, 1), BigDecimal("10"))
    val txn5 = createTransaction("AMC Theaters 2", LocalDate.of(2022, 12, 10), BigDecimal("22"))
    val txn6 = createTransaction("Target", LocalDate.of(2022, 1, 1), BigDecimal("20"))
    val txn7 =
      createTransaction("Universe", LocalDate.now(), BigDecimal("10")).let {
        transactionRepository.save(it.copy(categoryId = cat7.id))
      }

    transactions = listOf(txn1, txn2, txn3, txn4, txn5, txn6, txn7)

    createRule(categoryId = cat3.id, regex = "Target")
    createRule(categoryId = cat1.id, regex = "Target", minAmount = BigDecimal("90"))
    createRule(categoryId = cat2.id, regex = "Target", maxAmount = BigDecimal("15"))
    createRule(categoryId = cat6.id, regex = "AMC.*")
    createRule(categoryId = cat4.id, regex = "AMC.*", startDate = LocalDate.of(2022, 11, 1))
    createRule(categoryId = cat5.id, regex = "AMC.*", endDate = LocalDate.of(2022, 4, 1))
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
        .shouldBeRight()
    assertThat(result[1]).hasFieldOrPropertyWithValue("categoryId", categories[0].id)
    assertThat(result[0]).hasFieldOrPropertyWithValue("categoryId", categories[1].id)
    assertThat(result[5]).hasFieldOrPropertyWithValue("categoryId", categories[2].id)
    assertThat(result[4]).hasFieldOrPropertyWithValue("categoryId", categories[3].id)
    assertThat(result[2]).hasFieldOrPropertyWithValue("categoryId", categories[4].id)
    assertThat(result[3]).hasFieldOrPropertyWithValue("categoryId", categories[5].id)
    assertThat(result[6]).hasFieldOrPropertyWithValue("categoryId", null)
  }

  private fun validateCategoryApplied(
    txn: Transaction,
    categoryId: TypedId<CategoryId>,
    ruleId: TypedId<AutoCategorizeRuleId>
  ) {
    assertThat(txn).hasFieldOrPropertyWithValue("categoryId", categoryId)
    assertThat(lastRuleAppliedRepository.findByUserIdAndTransactionId(1L, txn.id))
      .isNotNull
      .hasFieldOrPropertyWithValue("ruleId", ruleId)
  }
}
