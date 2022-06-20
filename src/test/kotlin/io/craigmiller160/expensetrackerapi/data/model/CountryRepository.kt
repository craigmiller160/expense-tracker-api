package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import org.springframework.data.jpa.repository.JpaRepository

interface CountryRepository : JpaRepository<Country, TypedId<CountryId>>
