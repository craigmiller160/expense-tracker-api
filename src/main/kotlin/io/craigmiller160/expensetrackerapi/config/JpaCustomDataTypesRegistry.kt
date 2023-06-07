package io.craigmiller160.expensetrackerapi.config

import io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype.TypedIdJpaBasicType
import org.hibernate.boot.SessionFactoryBuilder
import org.hibernate.boot.spi.MetadataImplementor
import org.hibernate.boot.spi.SessionFactoryBuilderFactory
import org.hibernate.boot.spi.SessionFactoryBuilderImplementor

/** This is registered in META-INF/services/org.hibernate.boot.spi.SessionFactoryBuilderFactory */
class JpaCustomDataTypesRegistry : SessionFactoryBuilderFactory {
  init {
    println("HELLO WORLD") // TODO delete this
  }
  override fun getSessionFactoryBuilder(
      metadata: MetadataImplementor,
      defaultBuilder: SessionFactoryBuilderImplementor
  ): SessionFactoryBuilder {
    metadata.typeConfiguration.basicTypeRegistry.register(TypedIdJpaBasicType.INSTANCE)
    return defaultBuilder
  }
}
