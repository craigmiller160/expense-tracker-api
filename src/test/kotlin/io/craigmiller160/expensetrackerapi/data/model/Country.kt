package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.data.model.core.ImmutableTableEntity
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "countries")
class Country(var name: String = "") : ImmutableTableEntity<CountryId>()
