package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.service.CategoryService
import io.craigmiller160.expensetrackerapi.web.types.category.CategoryRequest
import io.craigmiller160.expensetrackerapi.web.types.category.CategoryResponse
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/categories")
class CategoryController(private val categoryService: CategoryService) {
  @ApiResponse(
    content =
      [
        Content(
          mediaType = "application/json",
          array = ArraySchema(schema = Schema(implementation = CategoryResponse::class)))])
  @GetMapping
  fun getAllCategories(): TryEither<List<CategoryResponse>> = categoryService.getAllCategories()

  @ApiResponse(
    content =
      [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = CategoryResponse::class))])
  @PostMapping
  fun createCategory(@RequestBody request: CategoryRequest): TryEither<CategoryResponse> =
    categoryService.createCategory(request)

  @ApiResponse(
    content =
      [Content(mediaType = "application/json", schema = Schema(implementation = Unit::class))])
  @PutMapping("/{categoryId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun updateCategory(
    @PathVariable("categoryId") categoryId: TypedId<CategoryId>,
    @RequestBody request: CategoryRequest
  ): TryEither<Unit> = categoryService.updateCategory(categoryId, request)

  @ApiResponse(
    content =
      [Content(mediaType = "application/json", schema = Schema(implementation = Unit::class))])
  @DeleteMapping("/{categoryId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteCategory(@PathVariable("categoryId") categoryId: TypedId<CategoryId>): TryEither<Unit> =
    categoryService.deleteCategory(categoryId)
}
