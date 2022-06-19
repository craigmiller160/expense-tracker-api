package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.BaseIntegrationTest
import java.time.ZonedDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

/** Special test class for validating that the TypedId & parent model types work. */
class ModelValidationTest : BaseIntegrationTest() {
  // TODO need testcontainers working for this
  @Autowired private lateinit var countryRepository: CountryRepository
  private val NOW = ZonedDateTime.now()
  @Test
  fun `immutable entity inserts but cannot be updated`() {
    val country = Country("USA")
    countryRepository.save(country)
    val dbCountry = countryRepository.findById(country.id).orElseThrow()
    assertThat(country.created).isAfterOrEqualTo(NOW)
    assertThat(dbCountry).isEqualTo(country)

    val newCountry = dbCountry.copy(name = "CAN")
    assertThat(newCountry.id).isEqualTo(country.id)

    assertThrows<IllegalStateException> { countryRepository.save(newCountry) }
  }

  @Test
  fun `mutable entity inserts & can be updated`() {
    TODO("Finish this")
  }
}
