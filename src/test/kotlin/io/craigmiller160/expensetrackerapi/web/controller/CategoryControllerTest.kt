package io.craigmiller160.expensetrackerapi.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.repository.CategoryRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.extension.flushAndClear
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testutils.AuthenticationHelper
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import io.craigmiller160.expensetrackerapi.utils.StringToColor
import io.craigmiller160.expensetrackerapi.web.types.category.CategoryRequest
import io.craigmiller160.expensetrackerapi.web.types.category.CategoryResponse
import javax.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@ExpenseTrackerIntegrationTest
class CategoryControllerTest
@Autowired
constructor(
  private val categoryRepository: CategoryRepository,
  private val transactionRepository: TransactionRepository,
  private val entityManager: EntityManager,
  private val dataHelper: DataHelper,
  private val mockMvc: MockMvc,
  private val objectMapper: ObjectMapper,
  private val authHelper: AuthenticationHelper
) {

  @Test
  fun getAllCategories() {
    val token = authHelper.primaryUser.token
    val cat1 = dataHelper.createCategory(authHelper.primaryUser.userId, "Category 1")
    val cat2 = dataHelper.createCategory(authHelper.primaryUser.userId, "Category 2")
    dataHelper.createCategory(authHelper.secondaryUser.userId, "Category 3")

    entityManager.flushAndClear()

    val expected = listOf(CategoryResponse.from(cat1), CategoryResponse.from(cat2))

    mockMvc
      .get("/categories") {
        secure = true
        header("Authorization", "Bearer $token")
      }
      .andExpect {
        status { isOk() }
        content { json(objectMapper.writeValueAsString(expected), true) }
      }
  }

  @Test
  fun createCategory() {
    val token = authHelper.primaryUser.token
    val request = CategoryRequest("The Category")

    val responseString =
      mockMvc
        .post("/categories") {
          secure = true
          header("Authorization", "Bearer $token")
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect { status { isOk() } }
        .andReturn()
        .response
        .contentAsString
    val response = objectMapper.readValue(responseString, CategoryResponse::class.java)

    assertThat(response).hasFieldOrPropertyWithValue("name", request.name)

    assertThat(categoryRepository.count()).isEqualTo(1)
    assertThat(categoryRepository.findById(response.id))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("name", request.name)
      .hasFieldOrPropertyWithValue("userId", authHelper.primaryUser.userId)
      .hasFieldOrPropertyWithValue("color", StringToColor.get(request.name))
  }

  @Test
  fun `createCategory - cannot use name Unknown`() {
    val token = authHelper.primaryUser.token
    val request = CategoryRequest("Unknown")

    mockMvc
      .post("/categories") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isBadRequest() } }
  }

  @Test
  fun `updateCategory - cannot use name Unknown`() {
    val token = authHelper.primaryUser.token
    val cat1 = dataHelper.createCategory(authHelper.primaryUser.userId, "Category 1")
    val request = CategoryRequest("Unknown")
    mockMvc
      .put("/categories/${cat1.id}") {
        secure = true
        header("Authorization", "Bearer $token")
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
      }
      .andExpect { status { isBadRequest() } }
  }

  @Test
  fun updateCategory() {
    val token = authHelper.primaryUser.token
    val cat1 = dataHelper.createCategory(authHelper.primaryUser.userId, "Category 1")
    val cat2 = dataHelper.createCategory(authHelper.primaryUser.userId, "Category 2")

    val request = CategoryRequest("Category B")
    val action: (TypedId<CategoryId>) -> Unit = { id ->
      mockMvc
        .put("/categories/$id") {
          secure = true
          header("Authorization", "Bearer $token")
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(request)
        }
        .andExpect { status { isNoContent() } }
    }

    action(cat1.uid)
    action(cat2.uid)

    val dbCat1 = categoryRepository.findById(cat1.uid).orElseThrow()
    assertThat(dbCat1)
      .hasFieldOrPropertyWithValue("name", "Category B")
      .hasFieldOrPropertyWithValue("color", StringToColor.get(request.name))

    val dbCat2 = categoryRepository.findById(cat2.uid).orElseThrow()
    assertThat(dbCat2)
      .hasFieldOrPropertyWithValue("name", "Category 2")
      .hasFieldOrPropertyWithValue("color", StringToColor.get(cat2.name))
  }

  @Test
  fun deleteCategory() {
    val token = authHelper.primaryUser.token
    val cat1 = dataHelper.createCategory(authHelper.primaryUser.userId, "Category 1")
    val cat2 = dataHelper.createCategory(authHelper.secondaryUser.userId, "Category 2")
    val cat3 = dataHelper.createCategory(authHelper.tertiaryUser.userId, "Category 3")
    val txn1 = dataHelper.createTransaction(authHelper.primaryUser.userId, cat1.uid)
    val txn2 = dataHelper.createTransaction(authHelper.primaryUser.userId, cat3.uid)

    val action: (TypedId<CategoryId>) -> Unit = { id ->
      mockMvc
        .delete("/categories/$id") {
          secure = true
          header("Authorization", "Bearer $token")
        }
        .andExpect { status { isNoContent() } }
    }

    action(cat1.uid)
    action(cat2.uid)

    assertThat(categoryRepository.findById(cat1.uid)).isEmpty
    assertThat(categoryRepository.findById(cat2.uid)).isPresent
    assertThat(transactionRepository.findById(txn1.uid))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", null)
    assertThat(transactionRepository.findById(txn2.uid))
      .isPresent
      .get()
      .hasFieldOrPropertyWithValue("categoryId", cat3.uid)
  }
}
