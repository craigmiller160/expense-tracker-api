package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.data.model.core.AbstractImmutableEntity

data class Country(val name: String) : AbstractImmutableEntity<CountryId>()
