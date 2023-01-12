package io.craigmiller160.expensetrackerapi.testutils

import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct
import org.keycloak.adapters.KeycloakConfigResolver
import org.keycloak.admin.client.CreatedResponseUtil
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Component

@Component
class AuthenticationHelper(configResolver: KeycloakConfigResolver) {
  companion object {
    const val ROLE_ACCESS = "access"
  }

  private val keycloakDeployment = configResolver.resolve(null)
  private val keycloak =
    KeycloakBuilder.builder()
      .serverUrl(keycloakDeployment.authServerBaseUrl)
      .realm("master")
      .clientId("admin-cli")
      .grantType("password")
      .username("admin")
      .password("admin")
      .build()

  private val defaultUsers = ConcurrentHashMap<String, String>()

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
    val response = keycloak.realm(keycloakDeployment.realm).users().create(user)
    val userId = CreatedResponseUtil.getCreatedId(response)

    val passwordCred =
      CredentialRepresentation().apply {
        isTemporary = false
        type = CredentialRepresentation.PASSWORD
        value = "password"
      }

    keycloak.realm(keycloakDeployment.realm).users().get(userId).resetPassword(passwordCred)
  }

  @PostConstruct
  fun setup() {
    createUser("myUser@gmail.com")
  }
}
