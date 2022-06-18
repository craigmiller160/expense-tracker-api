package io.craigmiller160.expensetrackerapi

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest @AutoConfigureMockMvc @ExtendWith(SpringExtension::class) class BaseIntegrationTest
