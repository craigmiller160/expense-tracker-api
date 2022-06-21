package io.craigmiller160.expensetrackerapi.config

import io.craigmiller160.expensetrackerapi.common.data.typedid.spring.TypedIdConverter
import io.craigmiller160.expensetrackerapi.common.data.typedid.spring.TypedIdSetConverter
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class SpringWebConfig : WebMvcConfigurer {
  // TODO delete all this if it doesn't work
  override fun addFormatters(registry: FormatterRegistry) {
    registry.addConverter(TypedIdConverter())
    registry.addConverter(TypedIdSetConverter())
  }
}
