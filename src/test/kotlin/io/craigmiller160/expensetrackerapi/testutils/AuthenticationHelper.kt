package io.craigmiller160.expensetrackerapi.testutils

import org.keycloak.adapters.KeycloakConfigResolver
import org.keycloak.admin.client.Keycloak
import org.springframework.stereotype.Component

@Component
class AuthenticationHelper(configResolver: KeycloakConfigResolver) {
  private val keycloakDeployment = configResolver.resolve(null)
  private val keycloak =
    Keycloak.getInstance(
      keycloakDeployment.authServerBaseUrl,
      keycloakDeployment.realm,
      "admin",
      "admin",
      keycloakDeployment.resourceName,
      keycloakDeployment.resourceCredentials["secret"].toString())
}
