package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.Category

data class CategoryResponse(val id: TypedId<CategoryId>, val name: String, val color: String) {
  companion object {
    fun from(category: Category): CategoryResponse =
      CategoryResponse(id = category.id, name = category.name, color = category.color)
  }
}
