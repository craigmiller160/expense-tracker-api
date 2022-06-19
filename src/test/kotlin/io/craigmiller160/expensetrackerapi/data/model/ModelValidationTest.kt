package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

/** Special test class for validating that the TypedId & parent model types work. */
class ModelValidationTest : BaseIntegrationTest() {
  @Autowired private lateinit var countryRepository: CountryRepository
  @Test
  fun `immutable entity inserts but cannot be updated`() {
    TODO("Finish this")
  }

  @Test
  fun `mutable entity inserts & can be updated`() {
    TODO("Finish this")
  }
}
