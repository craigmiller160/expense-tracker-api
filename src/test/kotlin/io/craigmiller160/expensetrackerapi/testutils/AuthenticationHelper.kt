package io.craigmiller160.expensetrackerapi.testutils

import javax.annotation.PostConstruct
import org.keycloak.adapters.KeycloakConfigResolver
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Component

@Component
class AuthenticationHelper(configResolver: KeycloakConfigResolver) {
  companion object {
    const val ROLE_ACCESS = "access"
  }
  private val keycloakDeployment = configResolver.resolve(null)
  private val keycloak =
    Keycloak.getInstance(
      keycloakDeployment.authServerBaseUrl,
      keycloakDeployment.realm,
      "admin",
      "admin",
      keycloakDeployment.resourceName,
      keycloakDeployment.resourceCredentials["secret"].toString())

  fun createUser(userName: String, roles: List<String> = listOf(ROLE_ACCESS)) {
    val user =
      UserRepresentation().apply {
        username = userName
        isEnabled = true
        isEmailVerified = true
        firstName = "First $userName"
        lastName = "Last $userName"
        email = userName
        realmRoles = roles
      }
    keycloak.realm(keycloakDeployment.realm).users().create(user)
  }

  @PostConstruct fun setup() {}
}
