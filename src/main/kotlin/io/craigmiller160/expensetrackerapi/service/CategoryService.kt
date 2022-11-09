package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.repository.CategoryRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.function.flatMapCatch
import io.craigmiller160.expensetrackerapi.utils.StringToColor
import io.craigmiller160.expensetrackerapi.web.types.CategoryRequest
import io.craigmiller160.expensetrackerapi.web.types.CategoryResponse
import io.craigmiller160.oauth2.service.OAuth2Service
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
  private val categoryRepository: CategoryRepository,
  private val transactionRepository: TransactionRepository,
  private val oAuth2Service: OAuth2Service
) {
  fun getAllCategories(): TryEither<List<CategoryResponse>> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return Either.catch {
      categoryRepository.findAllByUserIdOrderByName(userId).map { CategoryResponse.from(it) }
    }
  }

  fun createCategory(request: CategoryRequest): TryEither<CategoryResponse> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return Either.catch {
        categoryRepository.save(
          Category(name = request.name, userId = userId, color = StringToColor.get(request.name)))
      }
      .map { CategoryResponse.from(it) }
  }

  @Transactional
  fun updateCategory(categoryId: TypedId<CategoryId>, request: CategoryRequest): TryEither<Unit> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return Either.catch { categoryRepository.findByIdAndUserId(categoryId, userId) }
      .flatMapCatch { nullableCategory ->
        nullableCategory?.let { category ->
          categoryRepository.save(
            category.copy(name = request.name, color = StringToColor.get(request.name)))
        }
      }
  }

  @Transactional
  fun deleteCategory(categoryId: TypedId<CategoryId>): TryEither<Unit> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return Either.catch {
        transactionRepository.removeCategoryFromAllTransactions(userId, categoryId)
      }
      .flatMapCatch { categoryRepository.deleteByIdAndUserId(categoryId, userId) }
  }
}
