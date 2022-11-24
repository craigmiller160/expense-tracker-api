package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.core.MutableEntity
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "categories")
class Category(var name: String = "", var userId: Long = 0, var color: String = "") :
  MutableEntity<CategoryId>()
