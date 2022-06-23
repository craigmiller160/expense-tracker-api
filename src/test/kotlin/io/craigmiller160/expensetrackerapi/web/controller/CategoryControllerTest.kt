package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.BaseIntegrationTest
import io.craigmiller160.expensetrackerapi.web.types.CategoryResponse
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get

class CategoryControllerTest : BaseIntegrationTest() {

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
    TODO()
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
