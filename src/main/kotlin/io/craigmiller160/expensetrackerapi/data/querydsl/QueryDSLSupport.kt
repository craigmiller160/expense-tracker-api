package io.craigmiller160.expensetrackerapi.data.querydsl

import com.querydsl.core.types.dsl.PathBuilderFactory
import com.querydsl.jpa.JPQLQuery
import javax.persistence.EntityManager
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.Querydsl
import org.springframework.stereotype.Component

@Component
class QueryDSLSupport(private val entityManager: EntityManager) {
  fun <T> applyPagination(query: JPQLQuery<T>, page: Pageable, entityType: Class<T>): JPQLQuery<T> =
    Querydsl(this.entityManager, PathBuilderFactory().create(entityType))
      .applyPagination(page, query)
}
