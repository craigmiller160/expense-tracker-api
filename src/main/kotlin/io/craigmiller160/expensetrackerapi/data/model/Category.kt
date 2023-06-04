package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.data.model.core.MutableTableEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "categories")
class Category(var name: String, var userId: TypedId<UserId>, var color: String) :
    MutableTableEntity<CategoryId>()
