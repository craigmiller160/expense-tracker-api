package io.craigmiller160.expensetrackerapi.testutils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.nimbusds.jwt.JWTClaimsSet
import java.time.ZonedDateTime
import java.util.UUID
import org.keycloak.representations.AccessToken

object KeycloakJwtUtils {
  private val objectMapper = jacksonObjectMapper()
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

    val claims =
      objectMapper
        .writeValueAsBytes(token)
        .let { objectMapper.readValue(it, jacksonTypeRef<Map<String, Any>>()) }
        .let { JWTClaimsSet.parse(it) }

    TODO()
  }

  class KeycloakJwtConfig {
    var userId: UUID = UUID.randomUUID()
    var expiration: ZonedDateTime = ZonedDateTime.now().plusHours(1)
  }
}
