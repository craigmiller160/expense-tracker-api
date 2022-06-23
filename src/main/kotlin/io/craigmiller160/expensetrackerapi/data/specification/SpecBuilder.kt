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

  fun <T> greaterThanOrEqualTo(value: Comparable<*>?, fieldName: String): Specification<T> =
      value?.let { nonNullValue ->
        Specification.where { root, query, builder ->
          builder.greaterThanOrEqualTo<Comparable<*>>(
              root.get<Comparable<*>>(fieldName), nonNullValue)
        }
      }
          ?: emptySpec()

  fun <T> lessThanOrEqualTo(value: Comparable<*>?, fieldName: String): Specification<T> =
      value?.let { nonNullValue ->
        Specification.where { root, query, builder ->
          builder.lessThanOrEqualTo<Comparable<*>>(root.get<Comparable<*>>(fieldName), nonNullValue)
        }
      }
          ?: emptySpec()

  fun <T> `in`(value: Collection<*>?, fieldName: String): Specification<T> =
      value?.let { nonNullValue ->
        Specification.where { root, query, builder -> root.get<Any>(fieldName).`in`(nonNullValue) }
      }
          ?: emptySpec()
}
