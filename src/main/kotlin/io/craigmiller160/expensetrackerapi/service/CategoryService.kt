package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.error.BadRequestException
import io.craigmiller160.expensetrackerapi.data.constants.CategoryConstants
import io.craigmiller160.expensetrackerapi.data.model.Category
import io.craigmiller160.expensetrackerapi.data.repository.CategoryRepository
import io.craigmiller160.expensetrackerapi.data.repository.TransactionRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.function.flatMapCatch
import io.craigmiller160.expensetrackerapi.utils.StringToColor
import io.craigmiller160.expensetrackerapi.web.types.category.CategoryRequest
import io.craigmiller160.expensetrackerapi.web.types.category.CategoryResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
  private val categoryRepository: CategoryRepository,
  private val transactionRepository: TransactionRepository,
  private val authService: AuthorizationService
) {
  fun getAllCategories(): TryEither<List<CategoryResponse>> {
    //    val userId = oAuth2Service.getAuthenticatedUser().userId
    //    return Either.catch {
    //      categoryRepository.findAllByUserIdOrderByName(userId).map { CategoryResponse.from(it) }
    //    }
    TODO()
  }

  private fun validateRequest(request: CategoryRequest): TryEither<CategoryRequest> {
    if (CategoryConstants.UNKNOWN_CATEGORY_NAME == request.name) {
      return Either.Left(
        BadRequestException("Illegal category name: ${CategoryConstants.UNKNOWN_CATEGORY_NAME}"))
    }
    return Either.Right(request)
  }

  @Transactional
  fun createCategory(request: CategoryRequest): TryEither<CategoryResponse> {
    val userId = authService.getAuthUserId()
    return validateRequest(request)
      .flatMapCatch {
        categoryRepository.save(
          Category(name = request.name, userId = userId, color = StringToColor.get(request.name)))
      }
      .map { CategoryResponse.from(it) }
  }

  @Transactional
  fun updateCategory(categoryId: TypedId<CategoryId>, request: CategoryRequest): TryEither<Unit> {
    //    val userId = oAuth2Service.getAuthenticatedUser().userId
    //    return validateRequest(request)
    //      .flatMapCatch { categoryRepository.findByUidAndUserId(categoryId, userId) }
    //      .flatMapCatch { nullableCategory ->
    //        nullableCategory?.let { category ->
    //          categoryRepository.save(
    //            category.apply {
    //              name = request.name
    //              color = StringToColor.get(request.name)
    //            })
    //        }
    //      }
    TODO()
  }

  @Transactional
  fun deleteCategory(categoryId: TypedId<CategoryId>): TryEither<Unit> {
    //    val userId = oAuth2Service.getAuthenticatedUser().userId
    //    return Either.catch {
    //        transactionRepository.removeCategoryFromAllTransactions(userId, categoryId)
    //      }
    //      .flatMapCatch { categoryRepository.deleteByUidAndUserId(categoryId, userId) }
    TODO()
  }
}
