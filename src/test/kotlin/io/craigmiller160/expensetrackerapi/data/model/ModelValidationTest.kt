package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.extension.flushAndClear
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import java.time.ZonedDateTime
import javax.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.InvalidDataAccessApiUsageException

/** Special test class for validating that the TypedId & parent model types work. */
@ExpenseTrackerIntegrationTest
class ModelValidationTest
@Autowired
constructor(
  private val countryRepository: CountryRepository,
  private val residentRepository: ResidentRepository,
  private val entityManager: EntityManager
) {
  private val NOW = ZonedDateTime.now()
  @Test
  fun `immutable entity inserts but cannot be updated`() {
    val country = Country("USA")
    countryRepository.save(country)

    entityManager.flushAndClear()

    assertThat(country.created).isAfterOrEqualTo(NOW)

    val dbCountry = countryRepository.findById(country.id).orElseThrow()
    assertThat(dbCountry).isEqualTo(country)

    val newCountry = dbCountry.copy(name = "CAN")
    assertThat(newCountry.id).isEqualTo(country.id)

    val ex =
      assertThrows<InvalidDataAccessApiUsageException> {
        countryRepository.saveAndFlush(newCountry)
      }
    assertThat(ex.cause).isNotNull.isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  fun `mutable entity inserts & can be updated`() {
    val resident = Resident("Bob")
    residentRepository.save(resident)

    assertThat(resident.created).isAfterOrEqualTo(NOW)
    assertThat(resident.updated).isAfterOrEqualTo(resident.created)

    val dbResident = residentRepository.findById(resident.id).orElseThrow()
    assertThat(dbResident).isEqualTo(resident)
    assertThat(dbResident.version).isEqualTo(1)

    Thread.sleep(100)

    val newResident = dbResident.copy(name = "Sally")
    residentRepository.save(newResident)

    entityManager.flushAndClear()

    val dbResident2 = residentRepository.findById(resident.id).orElseThrow()
    assertThat(dbResident2.name).isEqualTo(newResident.name)
    assertThat(dbResident2.updated).isAfter(resident.updated)
    assertThat(dbResident2.version).isEqualTo(2)
  }
}
