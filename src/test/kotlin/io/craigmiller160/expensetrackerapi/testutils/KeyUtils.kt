package io.craigmiller160.expensetrackerapi.testutils

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey

object KeyUtils {
  fun createKeyPair(): KeyPair {
    val keyPairGen = KeyPairGenerator.getInstance("RSA")
    return keyPairGen.genKeyPair()
  }

  fun createJwkSet(keyPair: KeyPair): JWKSet {
    val builder =
        RSAKey.Builder(keyPair.public as RSAPublicKey)
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(JWSAlgorithm.RS256)
            .keyID("oauth-jwt")
    return JWKSet(builder.build())
  }
}
