package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.data.model.core.AbstractMutableEntity

data class Resident(val name: String) : AbstractMutableEntity<ResidentId>()
