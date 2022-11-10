package io.craigmiller160.expensetrackerapi.web.types.transaction

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId

interface TransactionUpdateItem {
  val transactionId: TypedId<TransactionId>
}

interface TransactionAndCategoryUpdateItem : TransactionUpdateItem {
  val categoryId: TypedId<CategoryId>?
}

interface TransactionAndConfirmUpdateItem : TransactionUpdateItem {
  val confirmed: Boolean
}
