package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.data.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying

interface CategoryRepository : JpaRepository<Category, TypedId<CategoryId>> {
  fun findAllByUserIdOrderByName(userId: TypedId<UserId>): List<Category>

  fun findByUidAndUserId(id: TypedId<CategoryId>, userId: TypedId<UserId>): Category?

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  fun deleteByUidAndUserId(id: TypedId<CategoryId>, userId: TypedId<UserId>)

  fun existsByUidAndUserId(id: TypedId<CategoryId>, userId: TypedId<UserId>): Boolean
}
