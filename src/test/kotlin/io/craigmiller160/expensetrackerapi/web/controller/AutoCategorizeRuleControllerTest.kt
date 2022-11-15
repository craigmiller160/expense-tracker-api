package io.craigmiller160.expensetrackerapi.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepository
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testcore.OAuth2Extension
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRuleRequest
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRuleResponse
import java.math.BigDecimal
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.post

@ExpenseTrackerIntegrationTest
class AutoCategorizeRuleControllerTest
@Autowired
constructor(
  private val mockMvc: MockMvc,
  private val dataHelper: DataHelper,
  private val objectMapper: ObjectMapper,
  private val autoCategorizeRuleRepository: AutoCategorizeRuleRepository
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
      // TODO do I want to test response content?
    }

    testBadRequest(notExistCategory)
    testBadRequest(wrongUserCategory)
  }

  @Test
  fun updateRule() {
    TODO()
  }

  @Test
  fun updateRule_invalidCategory() {
    TODO()
  }

  @Test
  fun getRule() {
    TODO()
  }

  @Test
  fun deleteRule() {
    TODO()
  }

  @Test
  fun reOrderRule() {
    TODO()
  }
}
