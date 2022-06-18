package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.ImportTypeId
import io.craigmiller160.expensetrackerapi.data.model.core.AbstractImmutableEntity
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class TransactionImportType(
    val company: String,
    @Enumerated(EnumType.STRING) val fileFormat: ImportFileFormat
) : AbstractImmutableEntity<ImportTypeId>()
