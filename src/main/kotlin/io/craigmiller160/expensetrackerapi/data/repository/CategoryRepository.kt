package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying

interface CategoryRepository : JpaRepository<Category, TypedId<CategoryId>> {
  fun findAllByUserIdOrderByName(userId: Long): List<Category>

  fun findByRecordIdAndUserId(id: TypedId<CategoryId>, userId: Long): Category?

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  fun deleteByRecordIdAndUserId(id: TypedId<CategoryId>, userId: Long)

  fun existsByRecordIdAndUserId(id: TypedId<CategoryId>, userId: Long): Boolean
}
