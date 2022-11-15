package io.craigmiller160.expensetrackerapi.web.types.rules

import io.craigmiller160.expensetrackerapi.web.types.PageableRequest

data class AutoCategorizeRulePageRequest(override val pageNumber: Int, override val pageSize: Int) :
  PageableRequest
