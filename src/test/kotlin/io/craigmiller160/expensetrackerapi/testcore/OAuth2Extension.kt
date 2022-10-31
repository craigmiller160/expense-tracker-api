package io.craigmiller160.expensetrackerapi.testcore

import com.nimbusds.jose.jwk.JWKSet
import io.craigmiller160.expensetrackerapi.testutils.JwtUtils
import io.craigmiller160.expensetrackerapi.testutils.KeyUtils
import io.craigmiller160.oauth2.config.OAuth2Config
import java.security.KeyPair
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.mockito.kotlin.whenever
import org.springframework.test.context.junit.jupiter.SpringExtension

class OAuth2Extension : BeforeEachCallback {
  private val keyPair: KeyPair = KeyUtils.createKeyPair()
  private val jwkSet: JWKSet = KeyUtils.createJwkSet(keyPair)
  override fun beforeEach(ctx: ExtensionContext) {
    val springContext = SpringExtension.getApplicationContext(ctx)
    // TODO need to mock this bean
    val oAuth2Config = springContext.getBean(OAuth2Config::class.java)
    whenever(oAuth2Config.jwkSet).thenReturn(jwkSet)
    whenever(oAuth2Config.clientKey).thenReturn(JwtUtils.CLIENT_KEY)
    whenever(oAuth2Config.clientName).thenReturn(JwtUtils.CLIENT_NAME)
  }
}
