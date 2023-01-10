package io.craigmiller160.expensetrackerapi.web.controller

import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import org.junit.jupiter.api.Test

@ExpenseTrackerIntegrationTest
class SecurityTest {
  @Test
  fun `allows valid token with access role`() {
    TODO()
  }

  @Test
  fun `rejects valid token without access role`() {
    TODO()
  }

  @Test
  fun `allows healthcheck without token`() {
    TODO()
  }

  @Test
  fun `allows swagger without token`() {
    TODO()
  }
}
