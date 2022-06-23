package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.repository.CategoryRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.web.types.CategoryRequest
import io.craigmiller160.expensetrackerapi.web.types.CategoryResponse
import io.craigmiller160.oauth2.service.OAuth2Service
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val oAuth2Service: OAuth2Service
) {
  fun getAllCategories(): TryEither<List<CategoryResponse>> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return Either.catch {
      categoryRepository.findAllByUserId(userId).map { CategoryResponse.from(it) }
    }
  }

  fun createCategory(request: CategoryRequest): TryEither<CategoryResponse> = TODO()

  fun updateCategory(categoryId: TypedId<CategoryId>, request: CategoryRequest): TryEither<Unit> =
      TODO()

  fun deleteCategory(categoryId: TypedId<CategoryId>): TryEither<Unit> = TODO()
}
