package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.web.types.transaction.TransactionAndConfirmUpdateItem

data class TransactionToConfirm(
    override val transactionId: TypedId<TransactionId>,
    override val confirmed: Boolean
) : TransactionAndConfirmUpdateItem

// TODO needs validation
data class ConfirmTransactionsRequest(val transactionsToConfirm: Set<TransactionToConfirm>)
