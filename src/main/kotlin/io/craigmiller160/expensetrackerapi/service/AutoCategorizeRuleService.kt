package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import arrow.core.filterOrElse
import arrow.core.flatMap
import arrow.core.leftIfNull
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.error.BadRequestException
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepository
import io.craigmiller160.expensetrackerapi.data.repository.CategoryRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.function.flatMapCatch
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageRequest
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRulePageResponse
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRuleRequest
import io.craigmiller160.expensetrackerapi.web.types.rules.AutoCategorizeRuleResponse
import io.craigmiller160.oauth2.service.OAuth2Service
import java.math.BigDecimal
import java.time.LocalDate
import javax.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class AutoCategorizeRuleService(
  private val autoCategorizeRuleRepository: AutoCategorizeRuleRepository,
  private val categoryRepository: CategoryRepository,
  private val oAuth2Service: OAuth2Service
) {
  fun getAllRules(
    request: AutoCategorizeRulePageRequest
  ): TryEither<AutoCategorizeRulePageResponse> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return Either.catch { autoCategorizeRuleRepository.searchForRules(request, userId) }
      .map { AutoCategorizeRulePageResponse.from(it) }
  }

  private fun validateRule(rule: AutoCategorizeRule): TryEither<AutoCategorizeRule> {
    val startDate = rule.startDate ?: LocalDate.MIN
    val endDate = rule.endDate ?: LocalDate.MAX
    val minAmount = rule.minAmount ?: BigDecimal(Double.MIN_VALUE)
    val maxAmount = rule.maxAmount ?: BigDecimal(Double.MAX_VALUE)

    if (startDate > endDate) {
      return Either.Left(BadRequestException("Rule Start Date cannot be after Rule End Date"))
    }

    if (minAmount > maxAmount) {
      return Either.Left(
        BadRequestException("Rule Min Amount cannot be greater than Rule Max Amount"))
    }

    return Either.Right(rule)
  }

  @Transactional
  fun createRule(request: AutoCategorizeRuleRequest): TryEither<AutoCategorizeRuleResponse> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return validateCategory(request.categoryId, userId)
      .flatMapCatch { autoCategorizeRuleRepository.countAllByUserId(userId) }
      .map { count ->
        AutoCategorizeRule(
          userId = userId,
          categoryId = request.categoryId,
          ordinal = (count + 1).toInt(),
          regex = request.regex,
          startDate = request.startDate,
          endDate = request.endDate,
          minAmount = request.minAmount,
          maxAmount = request.maxAmount)
      }
      .flatMap { validateRule(it) }
      .flatMapCatch { rule -> autoCategorizeRuleRepository.save(rule) }
      .map { AutoCategorizeRuleResponse.from(it) }
  }

  private fun validateCategory(categoryId: TypedId<CategoryId>, userId: Long): TryEither<Unit> =
    Either.catch { categoryRepository.existsByIdAndUserId(categoryId, userId) }
      .filterOrElse({ it }) { BadRequestException("Invalid Category: $categoryId") }
      .map { Unit }

  private fun getRuleIfValid(
    ruleId: TypedId<AutoCategorizeRuleId>,
    userId: Long
  ): TryEither<AutoCategorizeRule> =
    Either.catch { autoCategorizeRuleRepository.findByIdAndUserId(ruleId, userId) }
      .leftIfNull { BadRequestException("Invalid Rule: $ruleId") }

  @Transactional
  fun updateRule(
    ruleId: TypedId<AutoCategorizeRuleId>,
    request: AutoCategorizeRuleRequest
  ): TryEither<AutoCategorizeRuleResponse> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return validateCategory(request.categoryId, userId)
      .flatMap { getRuleIfValid(ruleId, userId) }
      .map { rule ->
        rule.copy(
          categoryId = request.categoryId,
          regex = request.regex,
          startDate = request.startDate,
          endDate = request.endDate,
          minAmount = request.minAmount,
          maxAmount = request.maxAmount)
      }
      .flatMap { validateRule(it) }
      .flatMapCatch { autoCategorizeRuleRepository.save(it) }
      .map { AutoCategorizeRuleResponse.from(it) }
  }

  fun getRule(ruleId: TypedId<AutoCategorizeRuleId>): TryEither<AutoCategorizeRuleResponse> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return getRuleIfValid(ruleId, userId).map { AutoCategorizeRuleResponse.from(it) }
  }

  @Transactional
  fun deleteRule(ruleId: TypedId<AutoCategorizeRuleId>): TryEither<Unit> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return getRuleIfValid(ruleId, userId).flatMapCatch { rule ->
      val count = autoCategorizeRuleRepository.countAllByUserId(userId)
      autoCategorizeRuleRepository.delete(rule)
      autoCategorizeRuleRepository.decrementOrdinals(userId, rule.ordinal, count.toInt())
    }
  }

  private fun validateOrdinal(userId: Long, ordinal: Int): TryEither<Int> =
    Either.catch { autoCategorizeRuleRepository.countAllByUserId(userId) }
      .filterOrElse({ it >= ordinal }) { BadRequestException("Invalid Ordinal: $ordinal") }
      .map { ordinal }

  @Transactional
  fun reOrderRule(ruleId: TypedId<AutoCategorizeRuleId>, ordinal: Int): TryEither<Unit> {
    val userId = oAuth2Service.getAuthenticatedUser().userId
    return validateOrdinal(userId, ordinal)
      .flatMap { getRuleIfValid(ruleId, userId) }
      .flatMap { rule ->
        if (rule.ordinal == ordinal) {
          Either.Right(rule)
        } else {
          changeOtherRuleOrdinals(userId, rule.ordinal, ordinal).flatMapCatch {
            autoCategorizeRuleRepository.save(rule.copy(ordinal = ordinal))
          }
        }
      }
      .map { Unit }
  }

  private fun changeOtherRuleOrdinals(
    userId: Long,
    oldOrdinal: Int,
    newOrdinal: Int
  ): TryEither<Unit> =
    Either.catch {
      if (oldOrdinal < newOrdinal) {
        autoCategorizeRuleRepository.decrementOrdinals(userId, oldOrdinal + 1, newOrdinal)
      } else {
        autoCategorizeRuleRepository.incrementOrdinals(userId, newOrdinal, oldOrdinal - 1)
      }
    }
}
