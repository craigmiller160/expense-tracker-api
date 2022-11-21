package io.craigmiller160.expensetrackerapi.extension

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import java.sql.ResultSet
import java.util.UUID

fun ResultSet.getUUID(columnName: String): UUID = UUID.fromString(getString(columnName))

fun <T> ResultSet.getTypedId(columnName: String): TypedId<T> = TypedId(getUUID(columnName))
