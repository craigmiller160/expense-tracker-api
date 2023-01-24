package io.craigmiller160.expensetrackerapi.testutils

import io.craigmiller160.testcontainers.common.core.AuthenticationHelper
import java.util.UUID
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
    val id = UUID.randomUUID().toString()
    val primaryUser = authHelper.createUser("primary_$id@gmail.com").let { authHelper.login(it) }
    val secondaryUser =
      authHelper.createUser("secondary_$id@gmail.com").let { authHelper.login(it) }
    val tertiaryUser = authHelper.createUser("tertiary_$id@gmail.com").let { authHelper.login(it) }
    return DefaultUsers(
      primaryUser = primaryUser, secondaryUser = secondaryUser, tertiaryUser = tertiaryUser)
  }
}
