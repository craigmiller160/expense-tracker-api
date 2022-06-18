package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typeid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typeid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.Category
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, TypedId<CategoryId>>
