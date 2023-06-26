package io.craigmiller160.expensetrackerapi.web.controller

import org.hamcrest.CoreMatchers
import org.springframework.test.web.servlet.ResultActionsDsl

object ControllerValidationSupport {
  fun <T> validate(
      config: ControllerValidationConfig<T>,
      setup: (ControllerValidationConfig<T>) -> ResultActionsDsl
  ) {
    setup(config).andExpect {
      status { isEqualTo(config.status) }
      content {
        if (config.status != 200) {
          jsonPath("$.message", CoreMatchers.equalTo(config.errorMessage))
        }
      }
    }
  }
}

data class ControllerValidationConfig<T>(
    val request: T,
    val status: Int,
    val errorMessage: String? = null
)
