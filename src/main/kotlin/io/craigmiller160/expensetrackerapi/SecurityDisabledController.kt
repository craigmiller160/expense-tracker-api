package io.craigmiller160.expensetrackerapi

import io.craigmiller160.oauth2.dto.AuthUserDto
import io.craigmiller160.oauth2.service.OAuth2Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// TODO delete this
@RestController
@RequestMapping("/oauth")
class SecurityDisabledController(private val oAuth2Service: OAuth2Service) {
  @GetMapping("/user") fun getAuthUser(): AuthUserDto = oAuth2Service.getAuthenticatedUser()
}
