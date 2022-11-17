package io.craigmiller160.expensetrackerapi.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepository
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testcore.OAuth2Extension
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageResponse
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRuleRequest
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRuleResponse
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
  private val entityManager: EntityManager
) {

  private lateinit var token: String

  private lateinit var cat1: Category
  private lateinit var cat2: Category

  @BeforeEach
  fun setup() {
    token = OAuth2Extension.createJwt()
    cat1 = dataHelper.createCategory(1L, "Entertainment")
    cat2 = dataHelper.createCategory(2L, "Food")
  }

  @Test
  fun getAllRules() {
    val rule1 = dataHelper.createRule(1L, cat1.id)
    val rule2 = dataHelper.createRule(1L, cat1.id)
    val rule3 = dataHelper.createRule(2L, cat2.id)

    val expectedResponse =
      AutoCategorizeRulePageResponse(
        pageNumber = 0,
        totalItems = 2,
        rules =
          listOf(AutoCategorizeRuleResponse.from(rule1), AutoCategorizeRuleResponse.from(rule2)))

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
    val rule1 = dataHelper.createRule(1L, cat1.id)
    val rule2 = dataHelper.createRule(1L, cat1.id)
    val rule3 = dataHelper.createRule(1L, cat3.id)

    val expectedResponse =
      AutoCategorizeRulePageResponse(
        pageNumber = 0,
        totalItems = 2,
        rules =
          listOf(AutoCategorizeRuleResponse.from(rule1), AutoCategorizeRuleResponse.from(rule2)))

    mockMvc
      .get("/categories/rules?pageNumber=0&pageSize=25&categoryId=${cat1.id}") {
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
      dataHelper.createRule(1L, cat1.id).let {
        autoCategorizeRuleRepository.save(it.copy(regex = "hello.*"))
      }
    val rule2 =
      dataHelper.createRule(1L, cat1.id).let {
        autoCategorizeRuleRepository.save(it.copy(regex = "Hello World"))
      }
    val rule3 = dataHelper.createRule(1L, cat1.id)
    entityManager.flush()

    val expectedResponse =
      AutoCategorizeRulePageResponse(
        pageNumber = 0,
        totalItems = 2,
        rules =
          listOf(AutoCategorizeRuleResponse.from(rule1), AutoCategorizeRuleResponse.from(rule2)))

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
  fun createRule_verifyApplyingRules() {
    TODO()
  }

  @Test
  fun createRule() {
    val request =
      AutoCategorizeRuleRequest(
        categoryId = cat1.id,
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

    val dbRecord = autoCategorizeRuleRepository.findById(response.id).orElseThrow()
    assertThat(AutoCategorizeRuleResponse.from(dbRecord)).isEqualTo(response)
  }

  @Test
  fun createRule_invalidRuleValues() {
    val baseRequest =
      AutoCategorizeRuleRequest(
        categoryId = cat1.id,
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
        categoryId = cat2.id,
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
    TODO()
  }

  @Test
  fun updateRule() {
    val rule = dataHelper.createRule(1L, cat1.id)
    val cat3 = dataHelper.createCategory(1L, "Hello")
    val request =
      AutoCategorizeRuleRequest(
        categoryId = cat3.id,
        regex = "Hello.*",
        startDate = LocalDate.of(2022, 1, 1),
        endDate = LocalDate.of(2022, 2, 2),
        minAmount = BigDecimal("10.0"),
        maxAmount = BigDecimal("20.0"))

    val expectedResponse =
      AutoCategorizeRuleResponse(
        id = rule.id,
        ordinal = rule.ordinal,
        categoryId = request.categoryId,
        regex = request.regex,
        startDate = request.startDate,
        endDate = request.endDate,
        minAmount = request.minAmount,
        maxAmount = request.maxAmount)

    mockMvc
      .put("/categories/rules/${rule.id}") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(expectedResponse), true) }
      }

    val dbRule = autoCategorizeRuleRepository.findById(rule.id).orElseThrow()
    assertThat(AutoCategorizeRuleResponse.from(dbRule)).isEqualTo(expectedResponse)
  }

  @Test
  fun updateRule_invalidRuleValues() {
    val rule = dataHelper.createRule(1L, cat1.id)
    val baseRequest =
      AutoCategorizeRuleRequest(
        categoryId = cat1.id,
        regex = ".*",
        startDate = LocalDate.of(2022, 1, 1),
        endDate = LocalDate.of(2022, 2, 2),
        minAmount = BigDecimal("10.00"),
        maxAmount = BigDecimal("20.00"))

    val operation: (AutoCategorizeRuleRequest, String) -> ResultActionsDsl = { request, message ->
      mockMvc
        .put("/categories/rules/${rule.id}") {
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
    val rule = dataHelper.createRule(1L, cat1.id)
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
        .put("/categories/rules/${rule.id}") {
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
    operation(baseRequest.copy(categoryId = cat2.id), "Invalid Category: ${cat2.id}")
  }

  @Test
  fun updateRule_invalidRule() {
    val rule = dataHelper.createRule(2L, cat1.id)
    val request =
      AutoCategorizeRuleRequest(
        categoryId = cat1.id,
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

    operation(rule.id, "Invalid Rule: ${rule.id}")
    val randomId = TypedId<AutoCategorizeRuleId>()
    operation(randomId, "Invalid Rule: $randomId")
  }

  @Test
  fun getRule() {
    val rule = dataHelper.createRule(1L, cat1.id)
    val expectedResponse = AutoCategorizeRuleResponse.from(rule)

    mockMvc
      .get("/categories/rules/${rule.id}") {
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
    val rule1 = dataHelper.createRule(1L, cat1.id)
    val rule2 = dataHelper.createRule(2L, cat2.id)

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

    operation(rule2.id, "Invalid Rule: ${rule2.id}")
    val randomId = TypedId<AutoCategorizeRuleId>()
    operation(randomId, "Invalid Rule: $randomId")
  }

  @Test
  fun deleteRule_verifyApplyingRules() {
    TODO()
  }

  @Test
  fun deleteRule() {
    val rule = dataHelper.createRule(1L, cat1.id)

    mockMvc
      .delete("/categories/rules/${rule.id}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isNoContent() } }

    assertThat(autoCategorizeRuleRepository.findById(rule.id)).isEmpty
  }

  @Test
  fun deleteRule_ordinalReOrdering() {
    val rules = createRulesForOrdinalValidation()

    mockMvc
      .delete("/categories/rules/${rules[2].id}") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isNoContent() } }

    assertThat(autoCategorizeRuleRepository.findById(rules[2].id)).isEmpty

    val expectedOrdinals =
      listOf(
        RuleIdAndOrdinal(rules[0].id, 1),
        RuleIdAndOrdinal(rules[1].id, 2),
        RuleIdAndOrdinal(rules[3].id, 3),
        RuleIdAndOrdinal(rules[4].id, 4))
    validateOrdinalsById(expectedOrdinals)
  }

  @Test
  fun reOrderRule_lowerOrdinal() {
    val rules = createRulesForOrdinalValidation()

    mockMvc
      .put("/categories/rules/${rules[3].id}/reOrder/2") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isNoContent() } }

    entityManager.flush()

    val expectedOrdinals =
      listOf(
        RuleIdAndOrdinal(rules[0].id, 1),
        RuleIdAndOrdinal(rules[3].id, 2),
        RuleIdAndOrdinal(rules[1].id, 3),
        RuleIdAndOrdinal(rules[2].id, 4),
        RuleIdAndOrdinal(rules[4].id, 5))
    validateOrdinalsById(expectedOrdinals)
  }

  @Test
  fun reOrderRule_higherOrdinal() {
    val rules = createRulesForOrdinalValidation()

    mockMvc
      .put("/categories/rules/${rules[1].id}/reOrder/4") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isNoContent() } }

    entityManager.flush()

    val expectedOrdinals =
      listOf(
        RuleIdAndOrdinal(rules[0].id, 1),
        RuleIdAndOrdinal(rules[2].id, 2),
        RuleIdAndOrdinal(rules[3].id, 3),
        RuleIdAndOrdinal(rules[1].id, 4),
        RuleIdAndOrdinal(rules[4].id, 5))
    validateOrdinalsById(expectedOrdinals)
  }

  @Test
  fun reOrderRule_noChange() {
    val rules = createRulesForOrdinalValidation()

    mockMvc
      .put("/categories/rules/${rules[1].id}/reOrder/2") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect { status { isNoContent() } }

    val expectedOrdinals =
      listOf(
        RuleIdAndOrdinal(rules[0].id, 1),
        RuleIdAndOrdinal(rules[1].id, 2),
        RuleIdAndOrdinal(rules[2].id, 3),
        RuleIdAndOrdinal(rules[3].id, 4),
        RuleIdAndOrdinal(rules[4].id, 5))
    validateOrdinalsById(expectedOrdinals)
  }

  @Test
  fun reOrderRule_invalidOrdinal() {
    val rules = createRulesForOrdinalValidation()

    mockMvc
      .put("/categories/rules/${rules[2].id}/reOrder/10") {
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
    dataHelper.createRule(1L, cat1.id)

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
    TODO()
  }

  private fun createRulesForOrdinalValidation(): List<AutoCategorizeRule> {
    val rules =
      (1..5).map { ordinal ->
        RuleAndOrdinal(rule = dataHelper.createRule(1L, cat1.id), ordinal = ordinal)
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
