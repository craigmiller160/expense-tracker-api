package io.craigmiller160.expensetrackerapi.data.querydsl

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.PathBuilderFactory
import com.querydsl.jpa.JPQLQuery
import io.craigmiller160.expensetrackerapi.data.model.YesNoFilter
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.Querydsl
import org.springframework.stereotype.Component

typealias QueryEnhancer<T> = (JPQLQuery<T>) -> JPQLQuery<T>

typealias QueryCondition<T> = (T) -> BooleanExpression

typealias WhereEnhancer = (BooleanBuilder) -> BooleanBuilder

@Component
class QueryDSLSupport(private val entityManager: EntityManager) {
  companion object {
    fun <T> andIfNotNull(
        builder: BooleanBuilder,
        value: T?,
        condition: QueryCondition<T>
    ): BooleanBuilder = value?.let { builder.and(condition(it)) } ?: builder

    fun <T> andIfNotNull(value: T?, condition: QueryCondition<T>): WhereEnhancer = { builder ->
      andIfNotNull(builder, value, condition)
    }

    fun andYesNoFilter(
        builder: BooleanBuilder,
        value: YesNoFilter,
        ifYes: BooleanExpression,
        ifNo: BooleanExpression
    ): BooleanBuilder =
        when (value) {
          YesNoFilter.YES -> ifYes
          YesNoFilter.NO -> ifNo
          YesNoFilter.ALL -> Expressions.TRUE
        }.let { builder.and(it) }

    fun andYesNoFilter(
        value: YesNoFilter,
        ifYes: BooleanExpression,
        ifNo: BooleanExpression
    ): WhereEnhancer = { builder -> andYesNoFilter(builder, value, ifYes, ifNo) }
  }
  fun <T> applyPagination(query: JPQLQuery<T>, page: Pageable, entityType: Class<T>): JPQLQuery<T> =
      Querydsl(this.entityManager, PathBuilderFactory().create(entityType))
          .applyPagination(page, query)

  fun <T> applyPagination(page: Pageable, entityType: Class<T>): QueryEnhancer<T> = { query ->
    applyPagination(query, page, entityType)
  }
}
