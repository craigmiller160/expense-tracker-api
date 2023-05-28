package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import io.craigmiller160.expensetrackerapi.data.projection.LastRuleAppliedForTransaction

interface LastRuleAppliedRepositoryCustom {
  fun getLastRuleDetailsForTransaction(
      userId: TypedId<UserId>,
      transactionId: TypedId<TransactionId>
  ): LastRuleAppliedForTransaction?
}
