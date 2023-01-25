package io.craigmiller160.expensetrackerapi.testutils

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.testcontainers.common.core.AuthenticationHelper

val AuthenticationHelper.TestUserWithToken.userTypedId: TypedId<UserId>
  get() = TypedId(userId)
