package io.craigmiller160.expensetrackerapi.web

import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.InitBinder

@ControllerAdvice
class ControllerDataBinding {
  @InitBinder
  fun initBinder(binder: WebDataBinder) {
    binder.initDirectFieldAccess()
  }
}
