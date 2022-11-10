package io.craigmiller160.expensetrackerapi.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.expensetrackerapi.testcore.ExpenseTrackerIntegrationTest
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc

@ExpenseTrackerIntegrationTest
class ReportControllerTest
@Autowired
constructor(
  private val mockMvc: MockMvc,
  private val objectMapper: ObjectMapper,
  private val dataHelper: DataHelper
) {
  @Test
  fun getReports() {
    TODO()
  }
}
