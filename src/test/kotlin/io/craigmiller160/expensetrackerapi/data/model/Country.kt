package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.data.model.core.ImmutableEntity
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "countries")
class Country(var name: String = "") : ImmutableEntity<CountryId>()
