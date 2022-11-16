package io.craigmiller160.expensetrackerapi.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
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
  // TODO add validations for start/end dates and min/max amounts

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

    val testBadRequest: (AutoCategorizeRuleRequest) -> ResultActionsDsl = { request ->
      mockMvc
        .post("/categories/rules") {
          secure = true
          header("Authorization", "Bearer $token")
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect { status { isBadRequest() } }
    }

    testBadRequest(notExistCategory)
    testBadRequest(wrongUserCategory)
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

    val operation: (AutoCategorizeRuleRequest) -> ResultActionsDsl = { request ->
      mockMvc
        .put("/categories/rules/${rule.id}") {
          secure = true
          header("Authorization", "Bearer $token")
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect { status { isBadRequest() } }
    }

    operation(baseRequest)
    operation(baseRequest.copy(categoryId = cat2.id))
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

    val operation: (TypedId<AutoCategorizeRuleId>) -> ResultActionsDsl = { id ->
      mockMvc
        .put("/categories/rules/$id") {
          secure = true
          header("Authorization", "Bearer $token")
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect { status { isBadRequest() } }
    }

    operation(rule.id)
    operation(TypedId())
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

    val operation: (TypedId<AutoCategorizeRuleId>) -> ResultActionsDsl = { id ->
      mockMvc
        .get("/categories/rules/$id") {
          secure = true
          header("Authorization", "Bearer $token")
        }
        .andExpect { status { isBadRequest() } }
    }

    operation(rule2.id)
    operation(TypedId())
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
  }

  @Test
  fun reOrderRule() {
    TODO()
  }
}
