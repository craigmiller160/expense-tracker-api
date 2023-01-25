package io.craigmiller160.expensetrackerapi.testutils

import io.craigmiller160.testcontainers.common.core.AuthenticationHelper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AuthenticationHelperConfig {
  @Bean
  fun authenticationHelper(): AuthenticationHelper {
    return AuthenticationHelper()
  }

  @Bean
  fun defaultUsers(authHelper: AuthenticationHelper): DefaultUsers {
    val primaryUser = authHelper.createUser("primary@gmail.com").let { authHelper.login(it) }
    val secondaryUser = authHelper.createUser("secondary@gmail.com").let { authHelper.login(it) }
    val tertiaryUser = authHelper.createUser("tertiary@gmail.com").let { authHelper.login(it) }
    return DefaultUsers(
      primaryUser = primaryUser, secondaryUser = secondaryUser, tertiaryUser = tertiaryUser)
  }
}
