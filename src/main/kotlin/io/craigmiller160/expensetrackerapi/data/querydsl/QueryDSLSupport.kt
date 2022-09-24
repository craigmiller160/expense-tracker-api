package io.craigmiller160.expensetrackerapi.data.querydsl

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.PathBuilderFactory
import com.querydsl.jpa.JPQLQuery
import javax.persistence.EntityManager
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.Querydsl
import org.springframework.stereotype.Component

typealias QueryEnhancer<T> = (JPQLQuery<T>) -> JPQLQuery<T>

typealias QueryCondition<T> = (T) -> BooleanBuilder

typealias WhereEnhancer = (BooleanBuilder) -> BooleanBuilder

@Component
class QueryDSLSupport(private val entityManager: EntityManager) {
  fun <T> applyPagination(query: JPQLQuery<T>, page: Pageable, entityType: Class<T>): JPQLQuery<T> =
    Querydsl(this.entityManager, PathBuilderFactory().create(entityType))
      .applyPagination(page, query)

  fun <T> applyPagination(page: Pageable, entityType: Class<T>): QueryEnhancer<T> = { query ->
    applyPagination(query, page, entityType)
  }

  fun <T> andIfNotNull(
    builder: BooleanBuilder,
    value: T?,
    condition: QueryCondition<T>
  ): BooleanBuilder = value?.let { condition(it) } ?: builder

  fun <T> andIfNotNull(value: T?, condition: QueryCondition<T>): WhereEnhancer = { builder ->
    andIfNotNull(builder, value, condition)
  }
}
