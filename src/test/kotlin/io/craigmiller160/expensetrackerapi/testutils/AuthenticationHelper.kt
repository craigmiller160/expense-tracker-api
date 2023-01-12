package io.craigmiller160.expensetrackerapi.testutils

import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct
import org.keycloak.adapters.KeycloakConfigResolver
import org.keycloak.admin.client.CreatedResponseUtil
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Component
class AuthenticationHelper(configResolver: KeycloakConfigResolver) {
  companion object {
    const val ROLE_ACCESS = "access"
  }

  private val restTemplate: RestTemplate = RestTemplate()
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

  fun login(userName: String, password: String): String {
    val clientId = keycloakDeployment.realm
    val clientSecret = keycloakDeployment.resourceCredentials["secret"].toString()
    val formData =
      LinkedMultiValueMap(
        mapOf(
          "grant_type" to listOf("password"),
          "client_id" to listOf(keycloakDeployment.realm),
          "username" to listOf(userName),
          "password" to listOf(password)))
    val basicAuth =
      "Basic ${Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())}"
    val headers = HttpHeaders()
    headers.add("Authorization", basicAuth)
    val entity = HttpEntity(formData, headers)
    return restTemplate
      .postForEntity(
        "${keycloakDeployment.authServerBaseUrl}/realms/apps-dev/protocol/openid-connect/token",
        entity,
        Map::class.java)
      .body
      ?.get("access_token")!!
      as String
  }

  @PostConstruct
  fun setup() {
    createUser("myUser@gmail.com")
  }
}
