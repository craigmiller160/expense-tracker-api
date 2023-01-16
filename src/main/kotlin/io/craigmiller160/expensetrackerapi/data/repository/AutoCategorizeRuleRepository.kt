package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import java.util.stream.Stream
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AutoCategorizeRuleRepository :
  JpaRepository<AutoCategorizeRule, TypedId<AutoCategorizeRuleId>>,
  AutoCategorizeRuleRepositoryCustom {
  fun countAllByUserId(userId: TypedId<UserId>): Long

  fun findByUidAndUserId(
    id: TypedId<AutoCategorizeRuleId>,
    userId: TypedId<UserId>
  ): AutoCategorizeRule?

  fun deleteByUidAndUserId(id: TypedId<AutoCategorizeRuleId>, userId: TypedId<UserId>)

  fun streamAllByUserIdOrderByOrdinal(userId: TypedId<UserId>): Stream<AutoCategorizeRule>

  @Query(
    """
    SELECT COALESCE(MAX(r.ordinal), 0)
    FROM AutoCategorizeRule r
    WHERE r.userId = :userId
  """)
  fun getMaxOrdinal(@Param("userId") userId: TypedId<UserId>): Int
}
