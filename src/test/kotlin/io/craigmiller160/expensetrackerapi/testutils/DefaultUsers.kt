package io.craigmiller160.expensetrackerapi.testutils

import io.craigmiller160.testcontainers.common.core.AuthenticationHelper

data class DefaultUsers(
  val primaryUser: AuthenticationHelper.TestUserWithToken,
  val secondaryUser: AuthenticationHelper.TestUserWithToken,
  val tertiaryUser: AuthenticationHelper.TestUserWithToken
)
