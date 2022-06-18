package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typeid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typeid.ids.CategoryId
import java.time.ZonedDateTime
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "categories")
@Suppress("JpaMissingIdInspection")
data class Category(
    val name: String,
    override val id: TypedId<CategoryId> = TypedId(),
    override val created: ZonedDateTime = ZonedDateTime.now(),
    override var updated: ZonedDateTime = ZonedDateTime.now(),
    override val version: Long = 1
) : AbstractEntity<CategoryId>()
