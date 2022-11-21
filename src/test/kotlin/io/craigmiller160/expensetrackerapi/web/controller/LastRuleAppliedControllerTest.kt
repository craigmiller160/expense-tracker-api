package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.LastRuleApplied
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepository
import io.craigmiller160.expensetrackerapi.data.repository.LastRuleAppliedRepository
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testcore.OAuth2Extension
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import java.math.BigDecimal
import java.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc

@ExpenseTrackerIntegrationTest
class LastRuleAppliedControllerTest(
  private val dataHelper: DataHelper,
  private val mockMvc: MockMvc,
  private val lastRuleAppliedRepository: LastRuleAppliedRepository,
  private val autoCategorizeRuleRepository: AutoCategorizeRuleRepository
) {
  private lateinit var token: String
  private lateinit var transaction: Transaction
  private lateinit var category: Category

  @BeforeEach
  fun setup() {
    token = OAuth2Extension.createJwt()
    transaction = dataHelper.createTransaction(1L)
    category = dataHelper.createCategory(1L, "Entertainment")
  }

  private fun createRule(): AutoCategorizeRule {
    val rule =
      autoCategorizeRuleRepository.saveAndFlush(
        AutoCategorizeRule(
          userId = 1L,
          categoryId = category.id,
          ordinal = 1,
          regex = ".*",
          startDate = LocalDate.of(2022, 1, 1),
          endDate = LocalDate.of(2022, 2, 2),
          minAmount = BigDecimal("10"),
          maxAmount = BigDecimal("20")))
    lastRuleAppliedRepository.save(
      LastRuleApplied(userId = 1L, transactionId = transaction.id, ruleId = rule.id))
    return rule
  }

  @Test
  fun `getLastAppliedRuleForTransaction - last applied exists`() {
    val rule = createRule()
    TODO()
  }

  @Test
  fun `getLastAppliedRuleForTransaction - last applied does not exist`() {
    TODO()
  }
}
