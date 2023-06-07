package io.craigmiller160.expensetrackerapi.common.data.typedid.jpatype;

import io.craigmiller160.expensetrackerapi.common.data.typedid.TypedId;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.jdbc.UUIDJdbcType;

public class TypedIdJpaType extends AbstractSingleColumnStandardBasicType<TypedId<?>> {
  public static final TypedIdJpaType INSTANCE = new TypedIdJpaType();

  public TypedIdJpaType() {
    super(UUIDJdbcType.INSTANCE, TypedIdJavaType.getINSTANCE());
  }

  @Override
  public String getName() {
    return "typed-id";
  }
}
