package io.craigmiller160.expensetrackerapi.config

import org.keycloak.adapters.KeycloakConfigResolver
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KeycloakConfig {
  @Bean fun keycloakConfigResolver(): KeycloakConfigResolver = KeycloakSpringBootConfigResolver()
}
