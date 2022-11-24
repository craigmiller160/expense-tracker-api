package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaType
import io.craigmiller160.expensetrackerapi.data.model.core.ImmutableLegacyEntity
import java.time.ZonedDateTime
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import org.hibernate.annotations.TypeDef

@Entity
@Table(name = "countries")
@TypeDef(defaultForType = TypedId::class, typeClass = TypedIdJpaType::class)
data class Country(
  val name: String,
  @Id override val id: TypedId<CountryId> = TypedId(),
  override val created: ZonedDateTime = ZonedDateTime.now()
) : ImmutableLegacyEntity<CountryId>
