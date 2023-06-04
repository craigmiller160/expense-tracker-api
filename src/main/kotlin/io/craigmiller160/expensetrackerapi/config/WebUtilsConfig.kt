package io.craigmiller160.expensetrackerapi.config

import io.craigmiller160.webutils.controller.RequestLogger
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@ComponentScan(
    basePackages = ["io.craigmiller160.webutils.controller", "io.craigmiller160.webutils.security"])
class WebUtilsConfig(private val requestLogger: RequestLogger) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    // TODO need to find a way to restore request logger
    //    registry.addInterceptor(requestLogger).addPathPatterns("/**")
  }
}
