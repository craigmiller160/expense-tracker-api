package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.data.model.core.MutableTableEntity
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "categories")
class Category(var name: String, var userId: UUID, var color: String) :
  MutableTableEntity<CategoryId>()
