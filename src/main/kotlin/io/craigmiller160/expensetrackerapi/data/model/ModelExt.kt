package io.craigmiller160.expensetrackerapi.data.model

import io.craigmiller160.expensetrackerapi.web.types.SortDirection
import org.springframework.data.domain.Sort

fun SortDirection.toSpringSortDirection(): Sort.Direction =
    when (this) {
      SortDirection.ASC -> Sort.Direction.ASC
      SortDirection.DESC -> Sort.Direction.DESC
    }
