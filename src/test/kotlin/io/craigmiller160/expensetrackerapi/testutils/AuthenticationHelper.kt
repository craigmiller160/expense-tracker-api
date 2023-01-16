package io.craigmiller160.expensetrackerapi.testutils

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import java.util.Base64
import java.util.UUID
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
    const val PRIMARY_USER = "PRIMARY_USER"
    const val SECONDARY_USER = "SECONDARY_USER"
    const val TERTIARY_USER = "TERTIARY_USER"
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

  private val defaultUsers = ConcurrentHashMap<String, TestUser>()
  val primaryUser: TestUser
    get() = defaultUsers[PRIMARY_USER]!!
  val secondaryUser: TestUser
    get() = defaultUsers[SECONDARY_USER]!!
  val tertiaryUser: TestUser
    get() = defaultUsers[TERTIARY_USER]!!

  fun createUser(userName: String, roles: List<String> = listOf(ROLE_ACCESS)): TestUser {
    val client =
      keycloak
        .realm(keycloakDeployment.realm)
        .clients()
        .findByClientId(keycloakDeployment.resourceName)
        .first()
    val accessRole =
      keycloak
        .realm(keycloakDeployment.realm)
        .clients()
        .get(client.id)
        .roles()
        .get(ROLE_ACCESS)
        .toRepresentation()
    val user =
      UserRepresentation().apply {
        username = userName
        isEnabled = true
        isEmailVerified = true
        firstName = "First $userName"
        lastName = "Last $userName"
        email = userName
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
    if (roles.contains(ROLE_ACCESS)) {
      keycloak
        .realm(keycloakDeployment.realm)
        .users()
        .get(userId)
        .roles()
        .clientLevel(client.id)
        .add(listOf(accessRole))
    }
    val token = login(userName, "password")
    return TestUser(userId = TypedId(userId), userName = userName, roles = roles, token = token)
  }

  fun login(userName: String, password: String): String {
    val clientId = keycloakDeployment.resourceName
    val clientSecret = keycloakDeployment.resourceCredentials["secret"].toString()
    val formData =
      LinkedMultiValueMap(
        mapOf(
          "grant_type" to listOf("password"),
          "client_id" to listOf(clientId),
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
    val id = UUID.randomUUID().toString()
    val primaryUser = createUser("primary_$id@gmail.com")
    val secondaryUser = createUser("secondary_$id@gmail.com")
    val tertiaryUser = createUser("tertiary_$id@gmail.com")
    defaultUsers += PRIMARY_USER to primaryUser
    defaultUsers += SECONDARY_USER to secondaryUser
    defaultUsers += TERTIARY_USER to tertiaryUser
  }

  data class TestUser(
    val userId: TypedId<UserId>,
    val userName: String,
    val roles: List<String>,
    val token: String
  )
}
