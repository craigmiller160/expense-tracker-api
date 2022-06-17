package io.craigmiller160.expensetrackerapi.common.data.typeid.ids

import io.craigmiller160.expensetrackerapi.common.data.typeid.TypedId
import java.util.UUID

class TransactionId(id: UUID = UUID.randomUUID()) : TypedId(id)
