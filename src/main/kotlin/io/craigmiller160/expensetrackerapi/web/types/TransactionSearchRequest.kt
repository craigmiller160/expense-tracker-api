package io.craigmiller160.expensetrackerapi.web.types

data class TransactionSearchRequest(override val pageNumber: Int, override val pageSize: Int) :
    PageableRequest
