package io.craigmiller160.expensetrackerapi.data.specification

import org.springframework.data.jpa.domain.Specification

object SpecBuilder {
  fun <T> emptySpec(): Specification<T> =
    Specification.where { _, _, builder -> builder.conjunction() }

  fun <T> equals(value: Any?, fieldName: String): Specification<T> =
    value?.let { nonNullValue ->
      Specification.where { root, _, builder ->
        builder.equal(root.get<Any>(fieldName), nonNullValue)
      }
    }
      ?: emptySpec()

  fun <T> isNull(fieldName: String): Specification<T> =
    Specification.where { root, _, builder -> builder.isNull(root.get<Any>(fieldName)) }

  fun <T> isNotNull(fieldName: String): Specification<T> =
    Specification.where { root, _, builder -> builder.isNotNull(root.get<Any>(fieldName)) }

  fun <T> greaterThanOrEqualTo(value: Comparable<*>?, fieldName: String): Specification<T> =
    value?.let { nonNullValue ->
      Specification.where { root, _, builder ->
        @Suppress("UPPER_BOUND_VIOLATED_WARNING", "TYPE_MISMATCH_WARNING")
        builder.greaterThanOrEqualTo<Comparable<*>>(
          root.get<Comparable<*>>(fieldName), nonNullValue)
      }
    }
      ?: emptySpec()

  fun <T> lessThanOrEqualTo(value: Comparable<*>?, fieldName: String): Specification<T> =
    value?.let { nonNullValue ->
      Specification.where { root, _, builder ->
        @Suppress("UPPER_BOUND_VIOLATED_WARNING", "TYPE_MISMATCH_WARNING")
        builder.lessThanOrEqualTo<Comparable<*>>(root.get<Comparable<*>>(fieldName), nonNullValue)
      }
    }
      ?: emptySpec()

  fun <T> `in`(value: Collection<*>?, fieldName: String): Specification<T> =
    value?.let { nonNullValue ->
      Specification.where { root, _, _ -> root.get<Any>(fieldName).`in`(nonNullValue) }
    }
      ?: emptySpec()
}
