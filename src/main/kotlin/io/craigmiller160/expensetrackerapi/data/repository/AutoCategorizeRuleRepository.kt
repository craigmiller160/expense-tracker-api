package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import java.util.stream.Stream
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AutoCategorizeRuleRepository :
  JpaRepository<AutoCategorizeRule, TypedId<AutoCategorizeRuleId>>,
  AutoCategorizeRuleRepositoryCustom {
  fun countAllByUserId(userId: Long): Long

  fun findByIdAndUserId(id: TypedId<AutoCategorizeRuleId>, userId: Long): AutoCategorizeRule?

  fun deleteByIdAndUserId(id: TypedId<AutoCategorizeRuleId>, userId: Long)

  fun streamAllByUserIdOrderByOrdinal(userId: Long): Stream<AutoCategorizeRule>

  @Query(
    """
    SELECT MAX(r.ordinal)
    FROM AutoCategorizeRule r
    WHERE r.userId = :userId
  """)
  fun getMaxOrdinal(@Param("userId") userId: Long): Int
}
