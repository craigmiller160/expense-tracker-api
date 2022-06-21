package io.craigmiller160.expensetrackerapi.data.specification

import org.springframework.data.jpa.domain.Specification

object SpecBuilder {
  private fun <T> emptySpec(): Specification<T> =
      Specification.where { root, query, builder -> builder.conjunction() }

  fun <T> equals(value: Any?, fieldName: String): Specification<T> =
      value?.let { nonNullValue ->
        Specification.where { root, query, builder ->
          builder.equal(root.get<Any>(fieldName), nonNullValue)
        }
      }
          ?: emptySpec()

  fun <T> `in`(value: Collection<*>?, fieldName: String): Specification<T> =
      value?.let { nonNullValue ->
        Specification.where { root, query, builder -> root.get<Any>(fieldName).`in`(nonNullValue) }
      }
          ?: emptySpec()
}
