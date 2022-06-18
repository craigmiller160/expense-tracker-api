package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.common.data.typedid.ids.ImportTypeId

data class ImportType(val name: String) : AbstractImmutableEntity<ImportTypeId>()
