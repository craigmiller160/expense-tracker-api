package io.craigmiller160.expensetrackerapi.data.utils

import io.craigmiller160.expensetrackerapi.common.crypto.SHA256
import io.craigmiller160.expensetrackerapi.common.utils.DateUtils
import java.math.BigDecimal
import java.time.LocalDate

object TransactionContentHash {
  fun hash(userId: Long, expenseDate: LocalDate, amount: BigDecimal, description: String): String =
    SHA256.hash("$userId${DateUtils.format(expenseDate)}${amount.toDouble()}$description")
}
