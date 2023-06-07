package io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype;

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.jdbc.UUIDJdbcType;
import org.springframework.stereotype.Component;

@Component
public class TypedIdJpaBasicType extends AbstractSingleColumnStandardBasicType<TypedId<?>> {
  public static final TypedIdJpaBasicType INSTANCE = new TypedIdJpaBasicType();

  public TypedIdJpaBasicType() {
    super(UUIDJdbcType.INSTANCE, TypedIdJavaType.getINSTANCE());
  }

  @Override
  public String getName() {
    return "typed-id";
  }
}
