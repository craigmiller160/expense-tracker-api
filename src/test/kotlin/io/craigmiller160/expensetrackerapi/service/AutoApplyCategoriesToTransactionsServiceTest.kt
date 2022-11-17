package io.craigmiller160.expensetrackerapi.service

import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ExpenseTrackerIntegrationTest
class AutoApplyCategoriesToTransactionsServiceTest
@Autowired
constructor(private val dataHelper: DataHelper) {

  @BeforeEach fun setup() {}

  @Test
  fun applyCategoriesToTransactions() {
    TODO()
  }
}
