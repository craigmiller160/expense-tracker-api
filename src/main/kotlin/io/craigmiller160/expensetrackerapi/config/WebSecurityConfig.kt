package io.craigmiller160.expensetrackerapi.config

import io.craigmiller160.spring.oauth2.security.JwtValidationFilterConfigurer
import io.craigmiller160.webutils.security.AuthEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    private val jwtFilterConfigurer: JwtValidationFilterConfigurer,
    private val authEntryPoint: AuthEntryPoint
) {
  @Bean
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
      http
          .csrf()
          .disable()
          .authorizeRequests()
          .antMatchers(*jwtFilterConfigurer.getInsecurePathPatterns())
          .permitAll()
          .anyRequest()
          .fullyAuthenticated()
          .and()
          .apply(jwtFilterConfigurer)
          .and()
          .exceptionHandling()
          .authenticationEntryPoint(authEntryPoint)
          .and()
          .sessionManagement()
          .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
          .and()
          .requiresChannel()
          .anyRequest()
          .requiresSecure()
          .and()
          .build()
}
