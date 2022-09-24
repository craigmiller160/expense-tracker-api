package io.craigmiller160.expensetrackerapi.config

import com.querydsl.jpa.impl.JPAQueryFactory
import javax.persistence.EntityManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QueryDSLConfig(private val entityManager: EntityManager) {
  @Bean fun queryFactory(): JPAQueryFactory = JPAQueryFactory(entityManager)
}
