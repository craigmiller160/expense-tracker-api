package io.craigmiller160.expensetrackerapi.web.types

data class GetPossibleDuplicatesRequest(override val pageNumber: Int, override val pageSize: Int) :
  PageableRequest
