package io.craigmiller160.expensetrackerapi.testutils

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.UUID

object JwtUtils {
  const val ROLES_CLAIM = "roles"
  const val CLIENT_KEY_CLAIM = "clientKey"
  const val FIRST_NAME_CLAIM = "firstName"
  const val LAST_NAME_CLAIM = "lastName"
  const val USER_ID_CLAIM = "userId"
  const val USERNAME = "user@example.com"
  val CLIENT_KEY = UUID.randomUUID()
  const val USER_ID = 1L
  const val CLIENT_NAME = "clientName"
  const val FIRST_NAME = "firstName"
  const val LAST_NAME = "lastName"
  val TOKEN_ID = UUID.randomUUID()

  fun createJwt(expMinutes: Long = 100): SignedJWT {
    val header = JWSHeader.Builder(JWSAlgorithm.RS256).build()

    val exp = ZonedDateTime.now(ZoneId.of("UTC")).plusMinutes(expMinutes)
    val expDate = Date.from(exp.toInstant())

    val claims =
        JWTClaimsSet.Builder()
            .jwtID(UUID.randomUUID().toString())
            .issueTime(Date())
            .subject(USERNAME)
            .expirationTime(expDate)
            .claim(ROLES_CLAIM, listOf<String>())
            .claim(CLIENT_KEY_CLAIM, CLIENT_KEY)
            .claim(CLIENT_NAME, CLIENT_NAME)
            .claim(FIRST_NAME_CLAIM, FIRST_NAME)
            .claim(LAST_NAME_CLAIM, LAST_NAME)
            .claim(USER_ID_CLAIM, USER_ID)
            .build()
    return SignedJWT(header, claims)
  }
}
