package io.craigmiller160.expensetrackerapi.data.repository

import io.craigmiller160.expensetrackerapi.common.data.typeid.TypedId
import io.craigmiller160.expensetrackerapi.common.data.typeid.ids.TransactionId
import io.craigmiller160.expensetrackerapi.data.model.Transaction
import org.springframework.data.jpa.repository.JpaRepository

interface TransactionRepository : JpaRepository<Transaction, TypedId<TransactionId>>
