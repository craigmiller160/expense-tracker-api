package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId

data class UpdateTransactionsRequest(
    val categorize: Set<TransactionAndCategory>,
    val confirm: Set<TypedId<TransactionId>>
)
