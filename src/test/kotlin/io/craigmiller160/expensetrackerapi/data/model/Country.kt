package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.data.model.core.AbstractImmutableEntity
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "countries")
data class Country(val name: String) : AbstractImmutableEntity<CountryId>()
