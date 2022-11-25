package io.craigmiller160.expensetrackerapi.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepository
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleViewRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.extension.flushAndClear
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testcore.OAuth2Extension
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageResponse
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRuleRequest
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRuleResponse
import io.craigmiller160.expensetrackerapi.web.types.rules.MaxOrdinalResponse
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*

@ExpenseTrackerIntegrationTest
class AutoCategorizeRuleControllerTest
@Autowired
constructor(
  private val mockMvc: MockMvc,
  private val dataHelper: DataHelper,
  private val objectMapper: ObjectMapper,
  private val autoCategorizeRuleRepository: AutoCategorizeRuleRepository,
  private val entityManager: EntityManager,
  private val transactionRepository: TransactionRepository,
  private val autoCategorizeRuleViewRepository: AutoCategorizeRuleViewRepository
) {

  private lateinit var token: String

  private lateinit var cat1: Category
  private lateinit var cat2: Category
  private lateinit var transaction: Transaction

  @BeforeEach
  fun setup() {
    token = OAuth2Extension.createJwt()
    cat1 = dataHelper.createCategory(1L, "Entertainment")
    cat2 = dataHelper.createCategory(2L, "Food")
    transaction = dataHelper.createTransaction(1L)
  }

  @Test
  fun getAllRules() {
    val rule1 = dataHelper.createRule(1L, cat1.recordId)
    val rule2 = dataHelper.createRule(1L, cat1.recordId)
    dataHelper.createRule(2L, cat2.recordId)

    val expectedResponse =
      AutoCategorizeRulePageResponse(
        pageNumber = 0,
        totalItems = 2,
        rules =
          listOf(
            AutoCategorizeRuleResponse.from(
              autoCategorizeRuleViewRepository.findById(rule1.recordId).orElseThrow()),
            AutoCategorizeRuleResponse.from(
              autoCategorizeRuleViewRepository.findById(rule2.recordId).orElseThrow())))

    mockMvc
      .get("/categories/rules?pageNumber=0&pageSize=25") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(expectedResponse), true) }
      }
  }

  @Test
  fun getAllRules_byCategoryId() {
    val cat3 = dataHelper.createCategory(1L, "Stuff")
    val rule1 = dataHelper.createRule(1L, cat1.recordId)
    val rule2 = dataHelper.createRule(1L, cat1.recordId)
    dataHelper.createRule(1L, cat3.recordId)

    val expectedResponse =
      AutoCategorizeRulePageResponse(
        pageNumber = 0,
        totalItems = 2,
        rules =
          listOf(
            AutoCategorizeRuleResponse.from(
              autoCategorizeRuleViewRepository.findById(rule1.recordId).orElseThrow()),
            AutoCategorizeRuleResponse.from(
              autoCategorizeRuleViewRepository.findById(rule2.recordId).orElseThrow())))

    mockMvc
      .get("/categories/rules?pageNumber=0&pageSize=25&categoryId=${cat1.recordId}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(expectedResponse), true) }
      }
  }

  @Test
  fun getAllRules_byRegex() {
    val rule1 =
      dataHelper.createRule(1L, cat1.recordId).let {
        autoCategorizeRuleRepository.save(it.apply { regex = "hello.*" })
      }
    val rule2 =
      dataHelper.createRule(1L, cat1.recordId).let {
        autoCategorizeRuleRepository.save(it.apply { regex = "Hello World" })
      }
    dataHelper.createRule(1L, cat1.recordId)
    entityManager.flushAndClear()

    val expectedResponse =
      AutoCategorizeRulePageResponse(
        pageNumber = 0,
        totalItems = 2,
        rules =
          listOf(
            AutoCategorizeRuleResponse.from(
              autoCategorizeRuleViewRepository.findById(rule1.recordId).orElseThrow()),
            AutoCategorizeRuleResponse.from(
              autoCategorizeRuleViewRepository.findById(rule2.recordId).orElseThrow())))

    mockMvc
      .get("/categories/rules?pageNumber=0&pageSize=25&regex=hello") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(expectedResponse), true) }
      }
  }

  @Test
  fun `createRule - with max ordinal`() {
    val rules = createRulesForOrdinalValidation()
    val request = AutoCategorizeRuleRequest(categoryId = cat1.recordId, regex = ".*", ordinal = 6)

    val responseString =
      mockMvc
        .post("/categories/rules") {
          secure = true
          header("Authorization", "Bearer $token")
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect { status { isOk() } }
        .andReturn()
        .response
        .contentAsString
    val response = objectMapper.readValue(responseString, AutoCategorizeRuleResponse::class.java)

    entityManager.flushAndClear()

    val expectedOrdinals =
      listOf(
        RuleIdAndOrdinal(rules[0].recordId, 1),
        RuleIdAndOrdinal(rules[1].recordId, 2),
        RuleIdAndOrdinal(rules[2].recordId, 3),
        RuleIdAndOrdinal(rules[3].recordId, 4),
        RuleIdAndOrdinal(rules[4].recordId, 5),
        RuleIdAndOrdinal(response.id, 6))
    validateOrdinalsById(expectedOrdinals)
  }

  @Test
  fun createRule_withOrdinal() {
    val rules = createRulesForOrdinalValidation()
    val request = AutoCategorizeRuleRequest(categoryId = cat1.recordId, regex = ".*", ordinal = 2)

    val responseString =
      mockMvc
        .post("/categories/rules") {
          secure = true
          header("Authorization", "Bearer $token")
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect { status { isOk() } }
        .andReturn()
        .response
        .contentAsString
    val response = objectMapper.readValue(responseString, AutoCategorizeRuleResponse::class.java)

    entityManager.flushAndClear()

    val expectedOrdinals =
      listOf(
        RuleIdAndOrdinal(rules[0].recordId, 1),
        RuleIdAndOrdinal(response.id, 2),
        RuleIdAndOrdinal(rules[1].recordId, 3),
        RuleIdAndOrdinal(rules[2].recordId, 4),
        RuleIdAndOrdinal(rules[3].recordId, 5),
        RuleIdAndOrdinal(rules[4].recordId, 6))
    validateOrdinalsById(expectedOrdinals)
  }

  @Test
  fun createRule_verifyApplyingRules() {
    val request = AutoCategorizeRuleRequest(categoryId = cat1.recordId, regex = ".*")

    mockMvc
      .post("/categories/rules") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isOk() } }

    assertThat(transactionRepository.findById(transaction.recordId))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", cat1.recordId)
  }

  @Test
  fun createRule() {
    val request =
      AutoCategorizeRuleRequest(
        categoryId = cat1.recordId,
        regex = ".*",
        startDate = LocalDate.of(2022, 1, 1),
        endDate = LocalDate.of(2022, 2, 2),
        minAmount = BigDecimal("10.00"),
        maxAmount = BigDecimal("20.00"))

    val responseString =
      mockMvc
        .post("/categories/rules") {
          secure = true
          header("Authorization", "Bearer $token")
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect { status { isOk() } }
        .andReturn()
        .response
        .contentAsString
    val response = objectMapper.readValue(responseString, AutoCategorizeRuleResponse::class.java)
    assertThat(response)
      .hasFieldOrPropertyWithValue("categoryId", request.categoryId)
      .hasFieldOrPropertyWithValue("ordinal", 1)
      .hasFieldOrPropertyWithValue("regex", request.regex)
      .hasFieldOrPropertyWithValue("startDate", request.startDate)
      .hasFieldOrPropertyWithValue("endDate", request.endDate)
      .hasFieldOrPropertyWithValue("minAmount", request.minAmount)
      .hasFieldOrPropertyWithValue("maxAmount", request.maxAmount)

    val dbRecord = autoCategorizeRuleViewRepository.findById(response.id).orElseThrow()
    assertThat(AutoCategorizeRuleResponse.from(dbRecord)).isEqualTo(response)
  }

  @Test
  fun createRule_invalidRuleValues() {
    val baseRequest =
      AutoCategorizeRuleRequest(
        categoryId = cat1.recordId,
        regex = ".*",
        startDate = LocalDate.of(2022, 1, 1),
        endDate = LocalDate.of(2022, 2, 2),
        minAmount = BigDecimal("10.00"),
        maxAmount = BigDecimal("20.00"))

    val operation: (AutoCategorizeRuleRequest, String) -> ResultActionsDsl = { request, message ->
      mockMvc
        .post("/categories/rules") {
          secure = true
          header("Authorization", "Bearer $token")
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect {
          status { isBadRequest() }
          jsonPath("$.message", equalTo(message))
        }
    }

    operation(
      baseRequest.copy(startDate = LocalDate.of(2022, 6, 1)),
      "Rule Start Date cannot be after Rule End Date")
    operation(
      baseRequest.copy(minAmount = BigDecimal("30.0")),
      "Rule Min Amount cannot be greater than Rule Max Amount")
  }

  @Test
  fun createRule_invalidCategory() {
    val notExistCategory =
      AutoCategorizeRuleRequest(
        categoryId = TypedId(),
        regex = ".*",
        startDate = LocalDate.of(2022, 1, 1),
        endDate = LocalDate.of(2022, 2, 2),
        minAmount = BigDecimal("10.00"),
        maxAmount = BigDecimal("20.00"))
    val wrongUserCategory =
      AutoCategorizeRuleRequest(
        categoryId = cat2.recordId,
        regex = ".*",
        startDate = LocalDate.of(2022, 1, 1),
        endDate = LocalDate.of(2022, 2, 2),
        minAmount = BigDecimal("10.00"),
        maxAmount = BigDecimal("20.00"))

    val testBadRequest: (AutoCategorizeRuleRequest, String) -> ResultActionsDsl =
      { request, message ->
        mockMvc
          .post("/categories/rules") {
            secure = true
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
          }
          .andExpect {
            status { isBadRequest() }
            jsonPath("$.message", equalTo(message))
          }
      }

    testBadRequest(notExistCategory, "Invalid Category: ${notExistCategory.categoryId}")
    testBadRequest(wrongUserCategory, "Invalid Category: ${wrongUserCategory.categoryId}")
  }

  @Test
  fun updateRule_verifyApplyingRules() {
    val cat3 = dataHelper.createCategory(1L, "Universe")
    val cat4 = dataHelper.createCategory(1L, "Time")
    val rule = dataHelper.createRule(1L, cat1.recordId)
    val rule2 = dataHelper.createRule(1L, cat3.recordId)
    dataHelper.createLastRuleApplied(1L, transaction.recordId, rule.recordId)
    val request = AutoCategorizeRuleRequest(categoryId = cat4.recordId, regex = ".*")

    entityManager.flushAndClear()

    mockMvc
      .put("/categories/rules/${rule2.recordId}") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isOk() } }

    assertThat(transactionRepository.findById(transaction.recordId))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", cat4.recordId)
  }

  @Test
  fun updateRule_withOrdinal() {
    val rules = createRulesForOrdinalValidation()
    val cat3 = dataHelper.createCategory(1L, "Hello")

    val request =
      AutoCategorizeRuleRequest(
        categoryId = cat3.recordId,
        regex = "Hello.*",
        ordinal = 2,
        startDate = LocalDate.of(2022, 1, 1),
        endDate = LocalDate.of(2022, 2, 2),
        minAmount = BigDecimal("10.0"),
        maxAmount = BigDecimal("20.0"))

    mockMvc
      .put("/categories/rules/${rules[3].recordId}") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isOk() } }

    entityManager.flushAndClear()

    val expectedOrdinals =
      listOf(
        RuleIdAndOrdinal(rules[0].recordId, 1),
        RuleIdAndOrdinal(rules[3].recordId, 2),
        RuleIdAndOrdinal(rules[1].recordId, 3),
        RuleIdAndOrdinal(rules[2].recordId, 4),
        RuleIdAndOrdinal(rules[4].recordId, 5))
    validateOrdinalsById(expectedOrdinals)
  }

  @Test
  fun updateRule() {
    val rule = dataHelper.createRule(1L, cat1.recordId)
    val cat3 = dataHelper.createCategory(1L, "Hello")
    val request =
      AutoCategorizeRuleRequest(
        categoryId = cat3.recordId,
        regex = "Hello.*",
        startDate = LocalDate.of(2022, 1, 1),
        endDate = LocalDate.of(2022, 2, 2),
        minAmount = BigDecimal("10.0"),
        maxAmount = BigDecimal("20.0"))

    val expectedResponse =
      AutoCategorizeRuleResponse(
        id = rule.recordId,
        ordinal = rule.ordinal,
        categoryId = request.categoryId,
        regex = request.regex,
        startDate = request.startDate,
        endDate = request.endDate,
        minAmount = request.minAmount,
        maxAmount = request.maxAmount,
        categoryName = cat3.name)

    mockMvc
      .put("/categories/rules/${rule.recordId}") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(expectedResponse), true) }
      }

    val dbRule = autoCategorizeRuleViewRepository.findById(rule.recordId).orElseThrow()
    assertThat(AutoCategorizeRuleResponse.from(dbRule)).isEqualTo(expectedResponse)
  }

  @Test
  fun updateRule_invalidRuleValues() {
    val rule = dataHelper.createRule(1L, cat1.recordId)
    val baseRequest =
      AutoCategorizeRuleRequest(
        categoryId = cat1.recordId,
        regex = ".*",
        startDate = LocalDate.of(2022, 1, 1),
        endDate = LocalDate.of(2022, 2, 2),
        minAmount = BigDecimal("10.00"),
        maxAmount = BigDecimal("20.00"))

    val operation: (AutoCategorizeRuleRequest, String) -> ResultActionsDsl = { request, message ->
      mockMvc
        .put("/categories/rules/${rule.recordId}") {
          secure = true
          header("Authorization", "Bearer $token")
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect {
          status { isBadRequest() }
          jsonPath("$.message", equalTo(message))
        }
    }

    operation(
      baseRequest.copy(startDate = LocalDate.of(2022, 6, 1)),
      "Rule Start Date cannot be after Rule End Date")
    operation(
      baseRequest.copy(minAmount = BigDecimal("30.0")),
      "Rule Min Amount cannot be greater than Rule Max Amount")
  }

  @Test
  fun updateRule_invalidCategory() {
    val rule = dataHelper.createRule(1L, cat1.recordId)
    val baseRequest =
      AutoCategorizeRuleRequest(
        categoryId = TypedId(),
        regex = "Hello.*",
        startDate = LocalDate.of(2022, 1, 1),
        endDate = LocalDate.of(2022, 2, 2),
        minAmount = BigDecimal("10.0"),
        maxAmount = BigDecimal("20.0"))

    val operation: (AutoCategorizeRuleRequest, String) -> ResultActionsDsl = { request, message ->
      mockMvc
        .put("/categories/rules/${rule.recordId}") {
          secure = true
          header("Authorization", "Bearer $token")
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect {
          status { isBadRequest() }
          jsonPath("$.message", equalTo(message))
        }
    }

    operation(baseRequest, "Invalid Category: ${baseRequest.categoryId}")
    operation(baseRequest.copy(categoryId = cat2.recordId), "Invalid Category: ${cat2.recordId}")
  }

  @Test
  fun updateRule_invalidRule() {
    val rule = dataHelper.createRule(2L, cat1.recordId)
    val request =
      AutoCategorizeRuleRequest(
        categoryId = cat1.recordId,
        regex = "Hello.*",
        startDate = LocalDate.of(2022, 1, 1),
        endDate = LocalDate.of(2022, 2, 2),
        minAmount = BigDecimal("10.0"),
        maxAmount = BigDecimal("20.0"))

    val operation: (TypedId<AutoCategorizeRuleId>, String) -> ResultActionsDsl = { id, message ->
      mockMvc
        .put("/categories/rules/$id") {
          secure = true
          header("Authorization", "Bearer $token")
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect {
          status { isBadRequest() }
          jsonPath("$.message", equalTo(message))
        }
    }

    operation(rule.recordId, "Invalid Rule: ${rule.recordId}")
    val randomId = TypedId<AutoCategorizeRuleId>()
    operation(randomId, "Invalid Rule: $randomId")
  }

  @Test
  fun getRule() {
    val rule = dataHelper.createRule(1L, cat1.recordId)
    val expectedResponse =
      AutoCategorizeRuleResponse.from(
        autoCategorizeRuleViewRepository.findById(rule.recordId).orElseThrow())

    mockMvc
      .get("/categories/rules/${rule.recordId}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(expectedResponse), true) }
      }
  }

  @Test
  fun getRule_invalidRule() {
    dataHelper.createRule(1L, cat1.recordId)
    val rule2 = dataHelper.createRule(2L, cat2.recordId)

    val operation: (TypedId<AutoCategorizeRuleId>, String) -> ResultActionsDsl = { id, message ->
      mockMvc
        .get("/categories/rules/$id") {
          secure = true
          header("Authorization", "Bearer $token")
        }
        .andExpect {
          status { isBadRequest() }
          jsonPath("$.message", equalTo(message))
        }
    }

    operation(rule2.recordId, "Invalid Rule: ${rule2.recordId}")
    val randomId = TypedId<AutoCategorizeRuleId>()
    operation(randomId, "Invalid Rule: $randomId")
  }

  @Test
  fun deleteRule_verifyApplyingRules() {
    val cat3 = dataHelper.createCategory(1L, "Hello")
    dataHelper.createRule(1L, cat1.recordId)
    val rule2 = dataHelper.createRule(1L, cat3.recordId)

    mockMvc
      .delete("/categories/rules/${rule2.recordId}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isNoContent() } }

    assertThat(transactionRepository.findById(transaction.recordId))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", cat1.recordId)
  }

  @Test
  fun deleteRule() {
    val rule = dataHelper.createRule(1L, cat1.recordId)

    mockMvc
      .delete("/categories/rules/${rule.recordId}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isNoContent() } }

    assertThat(autoCategorizeRuleRepository.findById(rule.recordId)).isEmpty
  }

  @Test
  fun deleteRule_ordinalReOrdering() {
    val rules = createRulesForOrdinalValidation()

    mockMvc
      .delete("/categories/rules/${rules[2].recordId}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isNoContent() } }

    entityManager.flushAndClear()

    assertThat(autoCategorizeRuleRepository.findById(rules[2].recordId)).isEmpty

    val expectedOrdinals =
      listOf(
        RuleIdAndOrdinal(rules[0].recordId, 1),
        RuleIdAndOrdinal(rules[1].recordId, 2),
        RuleIdAndOrdinal(rules[3].recordId, 3),
        RuleIdAndOrdinal(rules[4].recordId, 4))
    validateOrdinalsById(expectedOrdinals)
  }

  @Test
  fun reOrderRule_lowerOrdinal() {
    val rules = createRulesForOrdinalValidation()

    mockMvc
      .put("/categories/rules/${rules[3].recordId}/reOrder/2") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isNoContent() } }

    entityManager.flushAndClear()

    val expectedOrdinals =
      listOf(
        RuleIdAndOrdinal(rules[0].recordId, 1),
        RuleIdAndOrdinal(rules[3].recordId, 2),
        RuleIdAndOrdinal(rules[1].recordId, 3),
        RuleIdAndOrdinal(rules[2].recordId, 4),
        RuleIdAndOrdinal(rules[4].recordId, 5))
    validateOrdinalsById(expectedOrdinals)
  }

  @Test
  fun reOrderRule_higherOrdinal() {
    val rules = createRulesForOrdinalValidation()

    mockMvc
      .put("/categories/rules/${rules[1].recordId}/reOrder/4") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isNoContent() } }

    entityManager.flushAndClear()

    val expectedOrdinals =
      listOf(
        RuleIdAndOrdinal(rules[0].recordId, 1),
        RuleIdAndOrdinal(rules[2].recordId, 2),
        RuleIdAndOrdinal(rules[3].recordId, 3),
        RuleIdAndOrdinal(rules[1].recordId, 4),
        RuleIdAndOrdinal(rules[4].recordId, 5))
    validateOrdinalsById(expectedOrdinals)
  }

  @Test
  fun reOrderRule_noChange() {
    val rules = createRulesForOrdinalValidation()

    mockMvc
      .put("/categories/rules/${rules[1].recordId}/reOrder/2") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isNoContent() } }

    val expectedOrdinals =
      listOf(
        RuleIdAndOrdinal(rules[0].recordId, 1),
        RuleIdAndOrdinal(rules[1].recordId, 2),
        RuleIdAndOrdinal(rules[2].recordId, 3),
        RuleIdAndOrdinal(rules[3].recordId, 4),
        RuleIdAndOrdinal(rules[4].recordId, 5))
    validateOrdinalsById(expectedOrdinals)
  }

  @Test
  fun reOrderRule_invalidOrdinal() {
    val rules = createRulesForOrdinalValidation()

    mockMvc
      .put("/categories/rules/${rules[2].recordId}/reOrder/10") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isBadRequest() }
        jsonPath("$.message", equalTo("Invalid Ordinal: 10"))
      }
  }

  @Test
  fun reOrderRule_invalidRule() {
    dataHelper.createRule(1L, cat1.recordId)

    val id = TypedId<AutoCategorizeRuleId>()
    mockMvc
      .put("/categories/rules/$id/reOrder/1") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isBadRequest() }
        jsonPath("$.message", equalTo("Invalid Rule: $id"))
      }
  }

  @Test
  fun reOrderRule_verifyApplyingRules() {
    val rule = dataHelper.createRule(1L, cat1.recordId)
    mockMvc
      .put("/categories/rules/${rule.recordId}/reOrder/1") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isNoContent() } }

    entityManager.flushAndClear()

    assertThat(transactionRepository.findById(transaction.recordId))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", cat1.recordId)
  }

  @Test
  fun `getMaxOrdinal - with rules`() {
    repeat(3) { dataHelper.createRule(1L, cat1.recordId) }

    val expectedResponse = MaxOrdinalResponse(maxOrdinal = 3)

    mockMvc
      .get("/categories/rules/maxOrdinal") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(expectedResponse), true) }
      }
  }

  @Test
  fun `getMaxOrdinal - with no rules`() {
    val expectedResponse = MaxOrdinalResponse(maxOrdinal = 0)

    mockMvc
      .get("/categories/rules/maxOrdinal") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(expectedResponse), true) }
      }
  }

  private fun createRulesForOrdinalValidation(): List<AutoCategorizeRule> {
    val rules =
      (1..5).map { ordinal ->
        RuleAndOrdinal(rule = dataHelper.createRule(1L, cat1.recordId), ordinal = ordinal)
      }
    validateOrdinalsByRule(rules)
    return rules.map { it.rule }
  }

  private fun validateOrdinalsById(ruleIdsAndOrdinals: List<RuleIdAndOrdinal>) {
    val rulesAndOrdinals =
      ruleIdsAndOrdinals.map { ruleIdAndOrdinal ->
        RuleAndOrdinal(
          rule = autoCategorizeRuleRepository.findById(ruleIdAndOrdinal.ruleId).orElseThrow(),
          ordinal = ruleIdAndOrdinal.ordinal)
      }
    validateOrdinalsByRule(rulesAndOrdinals)
  }

  private fun validateOrdinalsByRule(rulesAndOrdinals: List<RuleAndOrdinal>) {
    rulesAndOrdinals.forEach { ruleAndOrdinal ->
      assertThat(ruleAndOrdinal.rule).hasFieldOrPropertyWithValue("ordinal", ruleAndOrdinal.ordinal)
    }
  }

  private data class RuleIdAndOrdinal(val ruleId: TypedId<AutoCategorizeRuleId>, val ordinal: Int)
  private data class RuleAndOrdinal(val rule: AutoCategorizeRule, val ordinal: Int)
}
