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

  fun findByUidAndUserId(id: TypedId<AutoCategorizeRuleId>, userId: Long): AutoCategorizeRule?

  fun deleteByUidAndUserId(id: TypedId<AutoCategorizeRuleId>, userId: Long)

  fun streamAllByUserIdOrderByOrdinal(userId: Long): Stream<AutoCategorizeRule>

  @Query(
    """
    SELECT COALESCE(MAX(r.ordinal), 0)
    FROM AutoCategorizeRule r
    WHERE r.userId = :userId
  """)
  fun getMaxOrdinal(@Param("userId") userId: Long): Int
}
