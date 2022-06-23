package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.BaseIntegrationTest
import io.craigmiller160.expensetrackerapi.data.repository.CategoryRepository
import io.craigmiller160.expensetrackerapi.web.types.CategoryRequest
import io.craigmiller160.expensetrackerapi.web.types.CategoryResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

class CategoryControllerTest : BaseIntegrationTest() {
  @Autowired private lateinit var categoryRepository: CategoryRepository

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
    TODO()
  }

  @Test
  fun deleteCategory() {
    TODO()
  }
}
