package io.craigmiller160.expensetrackerapi.testcore

import io.craigmiller160.oauth2.config.OAuth2Config
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MockConfig {
  @Bean
  fun oauth2Config(): OAuth2Config {
    return mock(OAuth2Config::class.java)
  }
}
