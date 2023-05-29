package io.craigmiller160.expensetrackerapi.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.LastRuleApplied
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepository
import io.craigmiller160.expensetrackerapi.data.repository.LastRuleAppliedRepository
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import io.craigmiller160.expensetrackerapi.testutils.DefaultUsers
import io.craigmiller160.expensetrackerapi.testutils.userTypedId
import io.craigmiller160.expensetrackerapi.web.types.rules.LastRuleAppliedResponse
import java.math.BigDecimal
import java.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@ExpenseTrackerIntegrationTest
class LastRuleAppliedControllerTest
@Autowired
constructor(
    private val dataHelper: DataHelper,
    private val mockMvc: MockMvc,
    private val lastRuleAppliedRepository: LastRuleAppliedRepository,
    private val autoCategorizeRuleRepository: AutoCategorizeRuleRepository,
    private val objectMapper: ObjectMapper,
    private val defaultUsers: DefaultUsers
) {
  private lateinit var token: String
  private lateinit var transaction: Transaction
  private lateinit var category: Category

  @BeforeEach
  fun setup() {
    token = defaultUsers.primaryUser.token
    transaction = dataHelper.createTransaction(defaultUsers.primaryUser.userTypedId)
    category = dataHelper.createCategory(defaultUsers.primaryUser.userTypedId, "Entertainment")
  }

  private fun createRule(): Pair<AutoCategorizeRule, LastRuleApplied> {
    val rule =
        autoCategorizeRuleRepository.saveAndFlush(
            AutoCategorizeRule(
                userId = defaultUsers.primaryUser.userTypedId,
                categoryId = category.uid,
                ordinal = 1,
                regex = ".*",
                startDate = LocalDate.of(2022, 1, 1),
                endDate = LocalDate.of(2022, 2, 2),
                minAmount = BigDecimal("10"),
                maxAmount = BigDecimal("20")))
    val lastApplied =
        lastRuleAppliedRepository.saveAndFlush(
            LastRuleApplied(
                userId = defaultUsers.primaryUser.userTypedId,
                transactionId = transaction.uid,
                ruleId = rule.uid))
    return rule to lastApplied
  }

  @Test
  fun `getLastAppliedRuleForTransaction - last applied exists`() {
    val (rule, lastApplied) = createRule()

    val response =
        LastRuleAppliedResponse(
            id = lastApplied.uid,
            ruleId = rule.uid,
            transactionId = transaction.uid,
            categoryId = rule.categoryId,
            categoryName = category.name,
            ordinal = rule.ordinal,
            regex = rule.regex,
            startDate = rule.startDate,
            endDate = rule.endDate,
            minAmount = rule.minAmount,
            maxAmount = rule.maxAmount)

    mockMvc
        .get("/transactions/rules/last-applied/${transaction.uid}") {
          secure = true
          header("Authorization", "Bearer $token")
        }
        .andExpect {
          status { isOk() }
          content { json(objectMapper.writeValueAsString(response), true) }
        }
  }

  @Test
  fun `getLastAppliedRuleForTransaction - last applied does not exist`() {
    mockMvc
        .get("/transactions/rules/last-applied/${transaction.uid}") {
          secure = true
          header("Authorization", "Bearer $token")
        }
        .andExpect { status { isNoContent() } }
  }
}
