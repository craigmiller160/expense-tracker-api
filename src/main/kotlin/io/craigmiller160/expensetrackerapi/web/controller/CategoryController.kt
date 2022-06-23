package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.web.types.CategoryRequest
import io.craigmiller160.expensetrackerapi.web.types.CategoryResponse
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/categories")
class CategoryController {
  @GetMapping fun getAllCategories(): TryEither<List<CategoryResponse>> = TODO()

  @PostMapping
  fun createCategory(@RequestBody request: CategoryRequest): TryEither<CategoryResponse> = TODO()

  @PutMapping("/{categoryId}")
  fun updateCategory(
      @PathVariable("categoryId") categoryId: TypedId<CategoryId>,
      @RequestBody request: CategoryRequest
  ): TryEither<CategoryResponse> = TODO()

  @DeleteMapping("/{categoryId}")
  fun deleteCategory(@PathVariable("categoryId") categoryId: TypedId<CategoryId>): TryEither<Unit> =
      TODO()
}
