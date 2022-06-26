package io.craigmiller160.expensetrackerapi.web.types

interface SortableRequest<T : Enum<T>> {
  val sortKey: T
  val sortDirection: SortDirection
}
