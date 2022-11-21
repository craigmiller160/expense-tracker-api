package io.craigmiller160.expensetrackerapi.service

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.function.TryEither
import io.craigmiller160.expensetrackerapi.web.types.rules.LastRuleAppliedResponse
import org.springframework.stereotype.Service

@Service
class LastRuleAppliedService {
  fun getLastAppliedRuleForTransaction(
    transactionId: TypedId<TransactionId>
  ): TryEither<LastRuleAppliedResponse?> = TODO()
}
