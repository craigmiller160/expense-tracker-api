package io.craigmiller160.expensetrackerapi.testutils

import java.time.ZonedDateTime
import java.util.UUID
import org.keycloak.representations.AccessToken

object KeycloakJwtUtils {
  fun createJwt(init: KeycloakJwtConfig.() -> Unit = {}): String {
    val config = KeycloakJwtConfig()
    config.init()
    val token =
      AccessToken()
        .apply {
          addAccess("expense-tracker-api").roles(setOf("access"))
          name = "Test User"
        }
        .iat(ZonedDateTime.now().toEpochSecond())
        .exp(config.expiration.toEpochSecond())
        .id(UUID.randomUUID().toString())
        .issuer("apps-dev")
        .audience("")
        .subject(config.userId.toString())
    TODO()
  }

  private fun zdtToLong(zdt: ZonedDateTime): Long = zdt.toEpochSecond()

  class KeycloakJwtConfig {
    var userId: UUID = UUID.randomUUID()
    var expiration: ZonedDateTime = ZonedDateTime.now().plusHours(1)
  }
}
