package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.AutoCategorizeRuleId
import io.craigmiller160.expensetrackerapi.data.model.AutoCategorizeRule
import java.util.stream.Stream
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
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
    UPDATE AutoCategorizeRule r
    SET r.ordinal = r.ordinal - 1, r.version = r.version + 1
    WHERE r.ordinal <= :maxOrdinal
    AND r.ordinal >= :minOrdinal
    AND r.userId = :userId
  """)
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  fun decrementOrdinals(
    @Param("userId") userId: Long,
    @Param("minOrdinal") minOrdinal: Int,
    @Param("maxOrdinal") maxOrdinal: Int
  )

  @Query(
    """
    UPDATE AutoCategorizeRule r
    SET r.ordinal = r.ordinal + 1, r.version = r.version + 1 
    WHERE r.ordinal <= :maxOrdinal
    AND r.ordinal >= :minOrdinal
    AND r.userId = :userId
  """)
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  fun incrementOrdinals(
    @Param("userId") userId: Long,
    @Param("minOrdinal") minOrdinal: Int,
    @Param("maxOrdinal") maxOrdinal: Int
  )
}
