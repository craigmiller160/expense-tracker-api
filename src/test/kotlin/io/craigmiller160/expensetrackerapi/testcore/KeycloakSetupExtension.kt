package io.craigmiller160.expensetrackerapi.testcore

import io.craigmiller160.expensetrackerapi.testutils.KeyUtils
import io.craigmiller160.expensetrackerapi.testutils.KeycloakJwtUtils
import java.security.KeyPair
import java.util.Base64
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.test.context.junit.jupiter.SpringExtension

class KeycloakSetupExtension : BeforeEachCallback {
  companion object {
    private val keyPair: KeyPair = KeyUtils.createKeyPair()
    init {
      val encodedPublicKey = Base64.getEncoder().encodeToString(keyPair.public.encoded)
      System.setProperty("keycloak.realm-public-key", encodedPublicKey)
    }

    fun createKeycloakJwt(init: KeycloakJwtUtils.KeycloakJwtConfig.() -> Unit = {}): String =
      KeycloakJwtUtils.createJwt(keyPair, init)
  }
  override fun beforeEach(ctx: ExtensionContext) {
    val springContext = SpringExtension.getApplicationContext(ctx)
  }
}
