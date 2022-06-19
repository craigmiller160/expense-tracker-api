package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.data.model.core.ImmutableEntity
import java.time.ZonedDateTime
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "countries")
data class Country(
    val name: String,
    @Id override val id: TypedId<CountryId> = TypedId(),
    override val created: ZonedDateTime = ZonedDateTime.now()
) : ImmutableEntity<CountryId>
