package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.data.model.core.ImmutableEntity
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "countries")
class Country : ImmutableEntity<CountryId>() {
  var name: String = ""
}
