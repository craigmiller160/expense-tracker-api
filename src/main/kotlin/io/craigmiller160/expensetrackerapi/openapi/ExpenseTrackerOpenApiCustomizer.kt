package io.craigmiller160.expensetrackerapi.openapi

import arrow.core.Either
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.OpenAPI
import org.springdoc.core.customizers.OpenApiCustomiser
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@Component
class ExpenseTrackerOpenApiCustomizer(
  private val requestMappingHandler: RequestMappingHandlerMapping
) : OpenApiCustomiser {
  // TODO how to handle response entities?
  // TODO need to support swagger overriding the method

  // ModelConverters.getInstance().resolveAsResolvedSchema(AnnotatedType(TransactionsPageResponse::class.java))
  // openApi.paths["/transactions"].post.responses["200"].content["*/*"].schema
  // requestMappingHandler.handlerMethods.entries.first().value.method.returnType

  // TODO cleanup null & exception handling
  override fun customise(openApi: OpenAPI) {
    val eitherReturnTypes =
      requestMappingHandler.handlerMethods.entries
        .filter { it.value.method.returnType == Either::class.java }
        .map { eitherReturnType ->
          val method = getMethod(eitherReturnType.key)
          val path = getPath(eitherReturnType.key)
          val operation =
            when (method) {
              RequestMethod.GET -> openApi.paths[path]!!.get
              RequestMethod.POST -> openApi.paths[path]!!.post
              RequestMethod.PUT -> openApi.paths[path]!!.put
              RequestMethod.DELETE -> openApi.paths[path]!!.delete
              else -> throw UnsupportedOperationException("$method")
            }

          val newSchema =
            ModelConverters.getInstance()
              .resolveAsResolvedSchema(AnnotatedType(eitherReturnType.value.method.returnType))

          // TODO what about non-200 responses?
          operation.responses["200"]?.let { response ->
            // TODO what about other content types?
            response.content["*/*"]?.schema = newSchema.schema
          }
        }
  }

  private fun getMethod(info: RequestMappingInfo): RequestMethod =
    info.methodsCondition.methods.first()
  // TODO better null handling below
  private fun getPath(info: RequestMappingInfo): String =
    info.pathPatternsCondition!!.patterns.first().patternString
}
