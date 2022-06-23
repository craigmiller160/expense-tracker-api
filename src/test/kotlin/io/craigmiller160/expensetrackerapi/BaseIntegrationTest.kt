package io.craigmiller160.expensetrackerapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.JWKSet
import io.craigmiller160.expensetrackerapi.testcontainers.PostgresContainer
import io.craigmiller160.expensetrackerapi.testutils.DataHelper
import io.craigmiller160.expensetrackerapi.testutils.JwtUtils
import io.craigmiller160.expensetrackerapi.testutils.KeyUtils
import io.craigmiller160.oauth2.config.OAuth2Config
import java.security.KeyPair
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ExtendWith(SpringExtension::class)
class BaseIntegrationTest {
  companion object {
    protected val keyPair: KeyPair = KeyUtils.createKeyPair()
    protected val jwkSet: JWKSet = KeyUtils.createJwkSet(keyPair)

    init {
      PostgresContainer.INSTANCE.start()
    }
  }

  @MockBean private lateinit var oAuth2Config: OAuth2Config
  @Autowired protected lateinit var mockMvc: MockMvc
  @Autowired protected lateinit var objectMapper: ObjectMapper
  @Autowired protected lateinit var dataHelper: DataHelper

  protected lateinit var token: String

  @BeforeEach
  fun baseSetup() {
    token = JwtUtils.createJwt(keyPair)
    whenever(oAuth2Config.jwkSet).thenReturn(jwkSet)
    whenever(oAuth2Config.clientKey).thenReturn(JwtUtils.CLIENT_KEY)
    whenever(oAuth2Config.clientName).thenReturn(JwtUtils.CLIENT_NAME)
  }
}
