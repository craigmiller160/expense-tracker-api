package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaType
import io.craigmiller160.expensetrackerapi.data.model.core.MutableLegacyEntity
import java.time.ZonedDateTime
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version
import org.hibernate.annotations.TypeDef

@Entity
@Table(name = "categories")
@TypeDef(defaultForType = TypedId::class, typeClass = TypedIdJpaType::class)
data class Category(
  val name: String,
  val userId: Long,
  val color: String,
  @Id override val id: TypedId<CategoryId> = TypedId(),
  override val created: ZonedDateTime = ZonedDateTime.now(),
  override var updated: ZonedDateTime = ZonedDateTime.now(),
  @Version override val version: Long = 1
) : MutableLegacyEntity<CategoryId>
