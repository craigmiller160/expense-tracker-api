package io.craigmiller160.expensetrackerapi

import io.craigmiller160.oauth2.dto.AuthUserDto
import io.craigmiller160.oauth2.service.OAuth2Service
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// TODO delete this
@Configuration
class SecurityDisablingConfig {
  @Bean
  fun oAuth2Service(): OAuth2Service =
    object : OAuth2Service {
      override fun getAuthenticatedUser(): AuthUserDto =
        AuthUserDto(
          userId = 1L,
          username = "craigmiller160@gmail.com",
          firstName = "Craig",
          lastName = "Miller",
          roles = listOf())

      override fun logout(): String = ""
    }
}