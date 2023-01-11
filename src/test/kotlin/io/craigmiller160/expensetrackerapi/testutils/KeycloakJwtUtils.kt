package io.craigmiller160.expensetrackerapi.testutils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.security.KeyPair
import java.time.ZonedDateTime
import java.util.UUID
import org.keycloak.representations.AccessToken

object KeycloakJwtUtils {
  private val objectMapper = jacksonObjectMapper()
  fun createJwt(keyPair: KeyPair, init: KeycloakJwtConfig.() -> Unit = {}): String {
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

    return objectMapper
      .writeValueAsBytes(token)
      .let { objectMapper.readValue(it, jacksonTypeRef<Map<String, Any>>()) }
      .let { JWTClaimsSet.parse(it) }
      .let {
        val header = JWSHeader.Builder(JWSAlgorithm.RS256).build()
        SignedJWT(header, it)
      }
      .let {
        val signer = RSASSASigner(keyPair.private)
        it.sign(signer)
        it.serialize()
      }
  }

  class KeycloakJwtConfig {
    var userId: UUID = UUID.randomUUID()
    var expiration: ZonedDateTime = ZonedDateTime.now().plusHours(1)
  }
}
