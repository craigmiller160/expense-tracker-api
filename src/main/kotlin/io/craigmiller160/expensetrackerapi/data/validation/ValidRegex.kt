package io.craigmiller160.expensetrackerapi.data.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Constraint(validatedBy = [ValidRegexValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidRegex(
    val message: String = "String is not valid regular expression",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
