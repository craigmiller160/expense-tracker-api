package io.craigmiller160.expensetrackerapi.config

import io.craigmiller160.springkeycloakoauth2resourceserver.security.KeycloakOAuth2ResourceServerProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.DefaultSecurityFilterChain

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    private val keycloakOAuth2ResourceServerProvider: KeycloakOAuth2ResourceServerProvider
) {
  @Bean
  fun configure(http: HttpSecurity): DefaultSecurityFilterChain =
      http
          .csrf { it.disable() }
          .oauth2ResourceServer(keycloakOAuth2ResourceServerProvider.provideWebMvc())
          .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
          .requiresChannel { it.anyRequest().requiresSecure() }
          .authorizeHttpRequests {
            it.requestMatchers(
                    "/actuator/health", "/v3/api-docs", "/v3/api-docs/*", "/swagger-ui/*")
                .permitAll()
                .requestMatchers("/**")
                .hasAnyRole("access")
          }
          .build()
}
