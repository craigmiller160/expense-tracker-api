package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.CategoryId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.UserId
import java.math.BigDecimal
import java.time.LocalDate

interface TransactionCommon {
  var uid: TypedId<TransactionId>
  var userId: TypedId<UserId>
  var expenseDate: LocalDate
  var description: String
  var amount: BigDecimal
  var confirmed: Boolean
  var categoryId: TypedId<CategoryId>?
}
