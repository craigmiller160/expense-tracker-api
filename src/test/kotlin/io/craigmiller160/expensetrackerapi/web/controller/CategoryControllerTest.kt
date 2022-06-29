package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.BaseIntegrationTest
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.repository.CategoryRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.web.types.CategoryRequest
import io.craigmiller160.expensetrackerapi.web.types.CategoryResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

class CategoryControllerTest : BaseIntegrationTest() {
  @Autowired private lateinit var categoryRepository: CategoryRepository
  @Autowired private lateinit var transactionRepository: TransactionRepository

  @Test
  fun getAllCategories() {
    val cat1 = dataHelper.createCategory(1L, "Category 1")
    val cat2 = dataHelper.createCategory(1L, "Category 2")
    val cat3 = dataHelper.createCategory(2L, "Category 3")

    entityManager.flush()

    val expected = listOf(CategoryResponse.from(cat1), CategoryResponse.from(cat2))

    mockMvc
        .get("/categories") {
          secure = true
          header("Authorization", "Bearer $token")
        }
        .andExpect {
          status { isOk() }
          content { json(objectMapper.writeValueAsString(expected)) }
        }
  }

  @Test
  fun createCategory() {
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
            .response.contentAsString
    val response = objectMapper.readValue(responseString, CategoryResponse::class.java)

    assertThat(response).hasFieldOrPropertyWithValue("name", request.name)

    assertThat(categoryRepository.count()).isEqualTo(1)
    assertThat(categoryRepository.findById(response.id))
        .isPresent
        .get()
        .hasFieldOrPropertyWithValue("name", request.name)
        .hasFieldOrPropertyWithValue("userId", 1L)
  }

  @Test
  fun updateCategory() {
    val cat1 = dataHelper.createCategory(1L, "Category 1")
    val cat2 = dataHelper.createCategory(2L, "Category 2")

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

    action(cat1.id)
    action(cat2.id)

    val dbCat1 = categoryRepository.findById(cat1.id).orElseThrow()
    assertThat(dbCat1).hasFieldOrPropertyWithValue("name", "Category B")

    val dbCat2 = categoryRepository.findById(cat2.id).orElseThrow()
    assertThat(dbCat2).hasFieldOrPropertyWithValue("name", "Category 2")
  }

  @Test
  fun deleteCategory() {
    val cat1 = dataHelper.createCategory(1L, "Category 1")
    val cat2 = dataHelper.createCategory(2L, "Category 2")
    val cat3 = dataHelper.createCategory(3L, "Category 3")
    val txn1 = dataHelper.createTransaction(1L, cat1.id)
    val txn2 = dataHelper.createTransaction(1L, cat3.id)

    val action: (TypedId<CategoryId>) -> Unit = { id ->
      mockMvc
          .delete("/categories/$id") {
            secure = true
            header("Authorization", "Bearer $token")
          }
          .andExpect { status { isNoContent() } }
    }

    action(cat1.id)
    action(cat2.id)

    assertThat(categoryRepository.findById(cat1.id)).isEmpty
    assertThat(categoryRepository.findById(cat2.id)).isPresent
    assertThat(transactionRepository.findById(txn1.id))
        .isPresent
        .get()
        .hasFieldOrPropertyWithValue("categoryId", null)
    assertThat(transactionRepository.findById(txn2.id))
        .isPresent
        .get()
        .hasFieldOrPropertyWithValue("categoryId", cat3.id)
  }
}
