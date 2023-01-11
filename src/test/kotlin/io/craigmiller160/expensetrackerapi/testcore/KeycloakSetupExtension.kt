package io.craigmiller160.expensetrackerapi.testcore

import io.craigmiller160.expensetrackerapi.testutils.KeyUtils
import java.security.KeyPair
import java.util.Base64
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class KeycloakSetupExtension : BeforeEachCallback {
  companion object {
    private val keyPair: KeyPair = KeyUtils.createKeyPair()
    init {
      val encodedPublicKey = Base64.getEncoder().encodeToString(keyPair.public.encoded)
      System.setProperty("keycloak.realm-public-key", encodedPublicKey)
    }

    fun createKeycloakJwt(): String = TODO()
  }
  override fun beforeEach(ctx: ExtensionContext) {}
}
