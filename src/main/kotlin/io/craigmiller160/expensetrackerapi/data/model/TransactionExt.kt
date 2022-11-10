package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.web.types.transaction.TransactionSortKey

fun TransactionSortKey.toColumnName(): String =
  when (this) {
    TransactionSortKey.EXPENSE_DATE -> "expenseDate"
  }
