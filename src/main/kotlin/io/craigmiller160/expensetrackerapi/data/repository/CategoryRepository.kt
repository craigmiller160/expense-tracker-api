package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying

interface CategoryRepository : JpaRepository<Category, TypedId<CategoryId>> {
  fun findAllByUserIdOrderByName(userId: Long): List<Category>

  fun findByUidAndUserId(id: TypedId<CategoryId>, userId: Long): Category?

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  fun deleteByUidAndUserId(id: TypedId<CategoryId>, userId: Long)

  fun existsByUidAndUserId(id: TypedId<CategoryId>, userId: Long): Boolean
}
