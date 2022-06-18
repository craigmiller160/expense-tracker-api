package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "categories")
data class Category(val name: String) : AbstractEntity<CategoryId>()
