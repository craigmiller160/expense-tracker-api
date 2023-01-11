package io.craigmiller160.expensetrackerapi.config

import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy

@Configuration
@EnableWebSecurity
class WebSecurityConfig : KeycloakWebSecurityConfigurerAdapter() {
  @Bean
  override fun sessionAuthenticationStrategy(): SessionAuthenticationStrategy =
    NullAuthenticatedSessionStrategy()

  override fun configure(http: HttpSecurity) {
    super.configure(http)
    http
      .csrf()
      .disable()
      .sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
      .and()
      .requiresChannel()
      .anyRequest()
      .requiresSecure()
      .and()
      .authorizeRequests()
      .antMatchers("/actuator/health", "/v3/api-docs", "/v3/api-docs/*", "/swagger-ui/*")
      .permitAll()
      .antMatchers("/**")
      .hasAnyRole("access")
  }

  override fun configure(auth: AuthenticationManagerBuilder) {
    val provider = keycloakAuthenticationProvider()
    provider.setGrantedAuthoritiesMapper(SimpleAuthorityMapper())
    auth.authenticationProvider(provider)
  }
}
