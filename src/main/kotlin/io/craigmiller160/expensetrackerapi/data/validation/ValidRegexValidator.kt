package io.craigmiller160.expensetrackerapi.data.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.util.regex.Pattern

class ValidRegexValidator : ConstraintValidator<ValidRegex, String> {
  override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
    if (value == null) {
      return true
    }

    return runCatching { Pattern.compile(value) }.isSuccess
  }
}
