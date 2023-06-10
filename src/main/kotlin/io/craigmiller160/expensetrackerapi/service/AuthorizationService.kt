package io.craigmiller160.expensetrackerapi.service

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class AuthorizationService {
  fun getAuthUserId(): TypedId<UserId> =
      (SecurityContextHolder.getContext().authentication.principal as Jwt).subject.let {
        TypedId(it)
      }
}
