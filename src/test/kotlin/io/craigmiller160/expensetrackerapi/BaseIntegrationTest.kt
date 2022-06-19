package io.craigmiller160.expensetrackerapi

import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.oauth2.config.OAuth2Config
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension::class)
class BaseIntegrationTest {
  @MockBean private lateinit var oAuth2Config: OAuth2Config
  @Autowired protected lateinit var mockMvc: MockMvc
  @Autowired protected lateinit var objectMapper: ObjectMapper
}
