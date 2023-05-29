package io.craigmiller160.expensetrackerapi.service

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.LastRuleApplied
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepository
import io.craigmiller160.expensetrackerapi.data.repository.LastRuleAppliedRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.extension.flushAndClear
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import io.craigmiller160.expensetrackerapi.testutils.DefaultUsers
import io.craigmiller160.expensetrackerapi.testutils.userTypedId
import io.kotest.assertions.arrow.core.shouldBeRight
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.EntityManager
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
    private val lastRuleAppliedRepository: LastRuleAppliedRepository,
    private val entityManager: EntityManager,
    private val defaultUsers: DefaultUsers
) {

  private var ruleCounter = 0

  private lateinit var transactions: List<Transaction>
  private lateinit var categories: List<Category>
  private lateinit var rules: List<AutoCategorizeRule>

  @BeforeEach
  fun setup() {
    ruleCounter = 0
    val cat0 = dataHelper.createCategory(defaultUsers.primaryUser.userTypedId, "Entertainment")
    val cat1 = dataHelper.createCategory(defaultUsers.primaryUser.userTypedId, "Food")
    val cat2 = dataHelper.createCategory(defaultUsers.primaryUser.userTypedId, "Bills")
    val cat3 = dataHelper.createCategory(defaultUsers.primaryUser.userTypedId, "Other")
    val cat4 = dataHelper.createCategory(defaultUsers.primaryUser.userTypedId, "Something")
    val cat5 = dataHelper.createCategory(defaultUsers.primaryUser.userTypedId, "Foo")
    val cat6 = dataHelper.createCategory(defaultUsers.primaryUser.userTypedId, "To Somewhere")

    categories = listOf(cat0, cat1, cat2, cat3, cat4, cat5, cat6)

    val txn0 = createTransaction("Target", LocalDate.of(2022, 1, 1), BigDecimal("10"))
    val txn1 = createTransaction("Target", LocalDate.of(2022, 1, 1), BigDecimal("100"))
    val txn2 = createTransaction("AMC Theaters", LocalDate.of(2022, 3, 10), BigDecimal("22"))
    val txn3 = createTransaction("AMC Theaters", LocalDate.of(2022, 6, 1), BigDecimal("10"))
    val txn4 = createTransaction("AMC Theaters 2", LocalDate.of(2022, 12, 10), BigDecimal("22"))
    val txn5 = createTransaction("Target", LocalDate.of(2022, 1, 1), BigDecimal("20"))
    val txn6 =
        createTransaction("Universe", LocalDate.now(), BigDecimal("10")).let {
          transactionRepository.save(it.apply { categoryId = cat6.uid })
        }

    transactions = listOf(txn0, txn1, txn2, txn3, txn4, txn5, txn6)

    val rule0 = createRule(categoryId = cat2.uid, regex = "Target")
    val rule1 = createRule(categoryId = cat0.uid, regex = "Target", minAmount = BigDecimal("90"))
    val rule2 = createRule(categoryId = cat1.uid, regex = "Target", maxAmount = BigDecimal("15"))
    val rule3 = createRule(categoryId = cat5.uid, regex = "AMC.*")
    val rule4 =
        createRule(categoryId = cat3.uid, regex = "AMC.*", startDate = LocalDate.of(2022, 11, 1))
    val rule5 =
        createRule(categoryId = cat4.uid, regex = "AMC.*", endDate = LocalDate.of(2022, 4, 1))
    rules = listOf(rule0, rule1, rule2, rule3, rule4, rule5)
  }

  private fun createTransaction(
      description: String,
      expenseDate: LocalDate,
      amount: BigDecimal
  ): Transaction =
      transactionRepository.save(
          Transaction(
              userId = defaultUsers.primaryUser.userTypedId,
              expenseDate = expenseDate,
              description = description,
              amount = amount))

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
              userId = defaultUsers.primaryUser.userTypedId,
              categoryId = categoryId,
              ordinal = ++ruleCounter,
              regex = regex,
              startDate = startDate,
              endDate = endDate,
              minAmount = minAmount,
              maxAmount = maxAmount))

  @Test
  fun applyCategoriesToTransactions() {
    lastRuleAppliedRepository.saveAndFlush(
        LastRuleApplied(
            userId = defaultUsers.primaryUser.userTypedId,
            ruleId = rules[0].uid,
            transactionId = transactions[6].uid))
    val result =
        applyCategoriesToTransactionsService
            .applyCategoriesToTransactions(defaultUsers.primaryUser.userTypedId, transactions)
            .shouldBeRight()

    entityManager.flushAndClear()

    validateCategoryApplied(transactions[1], categories[0].uid, rules[1].uid)
    validateCategoryApplied(transactions[0], categories[1].uid, rules[2].uid)
    validateCategoryApplied(transactions[5], categories[2].uid, rules[0].uid)
    validateCategoryApplied(transactions[4], categories[3].uid, rules[4].uid)
    validateCategoryApplied(transactions[2], categories[4].uid, rules[5].uid)
    validateCategoryApplied(transactions[3], categories[5].uid, rules[3].uid)
    validateCategoryApplied(transactions[6], null, null)
  }

  private fun validateCategoryApplied(
      txn: Transaction,
      categoryId: TypedId<CategoryId>?,
      ruleId: TypedId<AutoCategorizeRuleId>?
  ) {
    assertThat(transactionRepository.findById(txn.uid))
        .isPresent
        .get()
        .hasFieldOrPropertyWithValue("categoryId", categoryId)
    ruleId?.let { nonNullRuleId ->
      assertThat(
              lastRuleAppliedRepository.findByUserIdAndTransactionId(
                  defaultUsers.primaryUser.userTypedId, txn.uid))
          .isNotNull
          .hasFieldOrPropertyWithValue("ruleId", nonNullRuleId)
    }
        ?: assertThat(
                lastRuleAppliedRepository.findByUserIdAndTransactionId(
                    defaultUsers.primaryUser.userTypedId, txn.uid))
            .isNull()
  }
}
