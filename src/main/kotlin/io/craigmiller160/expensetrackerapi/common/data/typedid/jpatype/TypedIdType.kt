package io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId
import java.sql.Types
import org.hibernate.usertype.UserTypeSupport

class TypedIdType : UserTypeSupport<TypedId<*>>(TypedId::class.java, Types.BINARY)
