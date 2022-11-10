package io.craigmiller160.expensetrackerapi.web.types

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId

data class TransactionToConfirm(
  override val transactionId: TypedId<TransactionId>,
  override val confirmed: Boolean
) : TransactionAndConfirmUpdateItem

data class ConfirmTransactionsRequest(val transactionsToConfirm: Set<TransactionToConfirm>)
