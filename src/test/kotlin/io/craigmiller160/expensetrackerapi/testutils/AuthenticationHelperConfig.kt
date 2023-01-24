package io.craigmiller160.expensetrackerapi.testutils

import org.keycloak.adapters.KeycloakConfigResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AuthenticationHelperConfig {
  @Bean
  fun authenticationHelper(configResolver: KeycloakConfigResolver): AuthenticationHelper {
    return AuthenticationHelper(configResolver)
  }
}
