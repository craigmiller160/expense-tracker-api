package io.craigmiller160.expensetrackerapi.service

import arrow.core.Either
import arrow.core.filterOrElse
import arrow.core.flatMap
import arrow.core.leftIfNull
import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.common.error.BadRequestException
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRuleView
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleRepository
import io.craigmiller160.expensetrackerapi.data.repository.AutoCategorizeRuleViewRepository
import io.craigmiller160.expensetrackerapi.data.repository.CategoryRepository
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.function.flatMapCatch
import io.craigmiller160.expensetrackerapi.web.types.rules.*
import jakarta.transaction.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import org.springframework.stereotype.Service

@Service
class AutoCategorizeRuleService(
    private val autoCategorizeRuleRepository: AutoCategorizeRuleRepository,
    private val autoCategorizeRuleViewRepository: AutoCategorizeRuleViewRepository,
    private val categoryRepository: CategoryRepository,
    private val applyCategoriesToTransactionsService: ApplyCategoriesToTransactionsService,
    private val authService: AuthorizationService
) {
  fun getAllRules(
      request: AutoCategorizeRulePageRequest
  ): TryEither<AutoCategorizeRulePageResponse> {
    val userId = authService.getAuthUserId()
    return Either.catch { autoCategorizeRuleViewRepository.searchForRules(request, userId) }
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
    val userId = authService.getAuthUserId()
    return validateCategory(request.categoryId, userId)
        .flatMap {
          request.ordinal?.let { ordinal -> validateOrdinal(userId, ordinal, true) }
              ?: Either.catch { autoCategorizeRuleRepository.countAllByUserId(userId).toInt() + 1 }
        }
        .map { ordinal ->
          AutoCategorizeRule(
              userId = userId,
              categoryId = request.categoryId,
              ordinal = ordinal,
              regex = request.regex,
              startDate = request.startDate,
              endDate = request.endDate,
              minAmount = request.minAmount,
              maxAmount = request.maxAmount)
        }
        .flatMap { validateRule(it) }
        .flatMap { rule ->
          changeOtherRuleOrdinals(userId, Integer.MAX_VALUE, rule.ordinal).map { rule }
        }
        .flatMapCatch { rule -> autoCategorizeRuleRepository.save(rule) }
        .flatMap { rule ->
          applyCategoriesToTransactionsService
              .applyCategoriesToUnconfirmedTransactions(userId)
              .map { rule }
        }
        .flatMap { getRuleViewIfValid(it.uid, userId) }
        .map { AutoCategorizeRuleResponse.from(it) }
  }

  private fun validateCategory(
      categoryId: TypedId<CategoryId>,
      userId: TypedId<UserId>
  ): TryEither<Unit> =
      Either.catch { categoryRepository.existsByUidAndUserId(categoryId, userId) }
          .filterOrElse({ it }) { BadRequestException("Invalid Category: $categoryId") }
          .map { Unit }

  private fun getRuleIfValid(
      ruleId: TypedId<AutoCategorizeRuleId>,
      userId: TypedId<UserId>
  ): TryEither<AutoCategorizeRule> =
      Either.catch { autoCategorizeRuleRepository.findByUidAndUserId(ruleId, userId) }
          .leftIfNull { BadRequestException("Invalid Rule: $ruleId") }

  private fun getRuleViewIfValid(
      ruleId: TypedId<AutoCategorizeRuleId>,
      userId: TypedId<UserId>
  ): TryEither<AutoCategorizeRuleView> =
      Either.catch { autoCategorizeRuleViewRepository.findByUidAndUserId(ruleId, userId) }
          .leftIfNull { BadRequestException("Invalid Rule: $ruleId") }

  @Transactional
  fun updateRule(
      ruleId: TypedId<AutoCategorizeRuleId>,
      request: AutoCategorizeRuleRequest
  ): TryEither<AutoCategorizeRuleResponse> {
    val userId = authService.getAuthUserId()
    return validateCategory(request.categoryId, userId)
        .flatMap { getRuleIfValid(ruleId, userId) }
        .map { rule ->
          rule.apply {
            categoryId = request.categoryId
            regex = request.regex
            startDate = request.startDate
            endDate = request.endDate
            minAmount = request.minAmount
            maxAmount = request.maxAmount
          }
        }
        .flatMap { validateRule(it) }
        .flatMap { rule ->
          request.ordinal?.let { rawOrdinal ->
            validateOrdinal(userId, rawOrdinal).map { ordinal ->
              val oldOrdinal = rule.ordinal
              rule.apply { this.ordinal = ordinal } to oldOrdinal
            }
          }
              ?: Either.Right(rule to rule.ordinal)
        }
        .flatMap { (rule, oldOrdinal) ->
          changeOtherRuleOrdinals(userId, oldOrdinal, rule.ordinal, rule.uid).map { rule }
        }
        .flatMapCatch { autoCategorizeRuleRepository.save(it) }
        .flatMap { rule ->
          applyCategoriesToTransactionsService
              .applyCategoriesToUnconfirmedTransactions(userId)
              .map { rule }
        }
        .flatMap { getRuleViewIfValid(it.uid, userId) }
        .map { AutoCategorizeRuleResponse.from(it) }
  }

  fun getRule(ruleId: TypedId<AutoCategorizeRuleId>): TryEither<AutoCategorizeRuleResponse> {
    val userId = authService.getAuthUserId()
    return getRuleViewIfValid(ruleId, userId).map { AutoCategorizeRuleResponse.from(it) }
  }

  @Transactional
  fun deleteRule(ruleId: TypedId<AutoCategorizeRuleId>): TryEither<Unit> {
    val userId = authService.getAuthUserId()
    return getRuleIfValid(ruleId, userId)
        .flatMapCatch { rule ->
          val count = autoCategorizeRuleRepository.countAllByUserId(userId)
          autoCategorizeRuleRepository.delete(rule)
          autoCategorizeRuleRepository.decrementOrdinals(userId, rule.ordinal, count.toInt())
        }
        .flatMap {
          applyCategoriesToTransactionsService.applyCategoriesToUnconfirmedTransactions(userId)
        }
  }

  private fun validateOrdinal(
      userId: TypedId<UserId>,
      ordinal: Int,
      isCreate: Boolean = false
  ): TryEither<Int> =
      Either.catch { autoCategorizeRuleRepository.getMaxOrdinal(userId) }
          .map { maxOrdinal -> if (isCreate) maxOrdinal + 1 else maxOrdinal }
          .filterOrElse({ maxOrdinal -> maxOrdinal >= ordinal }) {
            BadRequestException("Invalid Ordinal: $ordinal")
          }
          .map { ordinal }

  @Transactional
  fun reOrderRule(ruleId: TypedId<AutoCategorizeRuleId>, ordinal: Int): TryEither<Unit> {
    val userId = authService.getAuthUserId()
    return validateOrdinal(userId, ordinal)
        .flatMap { getRuleIfValid(ruleId, userId) }
        .flatMap { rule ->
          changeOtherRuleOrdinals(userId, rule.ordinal, ordinal).flatMapCatch {
            autoCategorizeRuleRepository.save(rule.apply { this.ordinal = ordinal })
          }
        }
        .flatMap {
          applyCategoriesToTransactionsService.applyCategoriesToUnconfirmedTransactions(userId)
        }
  }

  private fun changeOtherRuleOrdinals(
      userId: TypedId<UserId>,
      oldOrdinal: Int,
      newOrdinal: Int,
      excludeId: TypedId<AutoCategorizeRuleId>? = null
  ): TryEither<Unit> =
      Either.catch {
        if (oldOrdinal == newOrdinal) {
          Either.Right(Unit)
        } else if (oldOrdinal < newOrdinal) {
          autoCategorizeRuleRepository.decrementOrdinals(
              userId, oldOrdinal + 1, newOrdinal, excludeId)
        } else {
          autoCategorizeRuleRepository.incrementOrdinals(
              userId, newOrdinal, oldOrdinal - 1, excludeId)
        }
      }

  fun getMaxOrdinal(): TryEither<MaxOrdinalResponse> {
    val userId = authService.getAuthUserId()
    return Either.catch { autoCategorizeRuleRepository.getMaxOrdinal(userId) }
        .map { MaxOrdinalResponse(it) }
  }
}
