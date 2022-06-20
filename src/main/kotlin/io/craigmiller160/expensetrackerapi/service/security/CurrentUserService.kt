package io.craigmiller160.expensetrackerapi.service.security

import io.craigmiller160.spring.oauth2.security.AuthenticatedUserDetails
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class CurrentUserService {
  fun getCurrentUser(): CurrentUser {
    val principal =
        SecurityContextHolder.getContext().authentication.principal as AuthenticatedUserDetails

    TODO()
  }
}
