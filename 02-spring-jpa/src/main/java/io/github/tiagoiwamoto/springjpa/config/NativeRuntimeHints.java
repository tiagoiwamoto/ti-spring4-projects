package io.github.tiagoiwamoto.springjpa.config;

import org.hibernate.bytecode.internal.none.BytecodeProviderImpl;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.type.PostgreSQLArrayJdbcType;
import org.hibernate.dialect.type.PostgreSQLArrayJdbcTypeConstructor;
import org.hibernate.dialect.type.PostgreSQLCastingInetJdbcType;
import org.hibernate.dialect.type.PostgreSQLCastingIntervalSecondJdbcType;
import org.hibernate.dialect.type.PostgreSQLCastingJsonArrayJdbcType;
import org.hibernate.dialect.type.PostgreSQLCastingJsonArrayJdbcTypeConstructor;
import org.hibernate.dialect.type.PostgreSQLCastingJsonJdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.hibernate.dialect.type.PostgreSQLInetJdbcType;
import org.hibernate.dialect.type.PostgreSQLIntervalSecondJdbcType;
import org.hibernate.dialect.type.PostgreSQLJsonArrayPGObjectJsonJdbcTypeConstructor;
import org.hibernate.dialect.type.PostgreSQLJsonArrayPGObjectJsonbJdbcTypeConstructor;
import org.hibernate.dialect.type.PostgreSQLJsonArrayPGObjectType;
import org.hibernate.dialect.type.PostgreSQLJsonPGObjectJsonType;
import org.hibernate.dialect.type.PostgreSQLJsonPGObjectJsonbType;
import org.hibernate.dialect.type.PostgreSQLOrdinalEnumJdbcType;
import org.hibernate.dialect.type.PostgreSQLStructCastingJdbcType;
import org.hibernate.dialect.type.PostgreSQLStructPGObjectJdbcType;
import org.hibernate.dialect.type.PostgreSQLUUIDJdbcType;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class NativeRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection()
                .registerType(PostgreSQLDialect.class, MemberCategory.values())
                .registerType(PostgreSQLArrayJdbcType.class, MemberCategory.values())
                .registerType(PostgreSQLArrayJdbcTypeConstructor.class, MemberCategory.values())
                .registerType(PostgreSQLCastingInetJdbcType.class, MemberCategory.values())
                .registerType(PostgreSQLCastingIntervalSecondJdbcType.class, MemberCategory.values())
                .registerType(PostgreSQLCastingJsonArrayJdbcType.class, MemberCategory.values())
                .registerType(PostgreSQLCastingJsonArrayJdbcTypeConstructor.class, MemberCategory.values())
                .registerType(PostgreSQLCastingJsonJdbcType.class, MemberCategory.values())
                .registerType(PostgreSQLEnumJdbcType.class, MemberCategory.values())
                .registerType(PostgreSQLInetJdbcType.class, MemberCategory.values())
                .registerType(PostgreSQLIntervalSecondJdbcType.class, MemberCategory.values())
                .registerType(PostgreSQLJsonArrayPGObjectJsonbJdbcTypeConstructor.class, MemberCategory.values())
                .registerType(PostgreSQLJsonArrayPGObjectJsonJdbcTypeConstructor.class, MemberCategory.values())
                .registerType(PostgreSQLJsonArrayPGObjectType.class, MemberCategory.values())
                .registerType(PostgreSQLJsonPGObjectJsonbType.class, MemberCategory.values())
                .registerType(PostgreSQLJsonPGObjectJsonType.class, MemberCategory.values())
                .registerType(PostgreSQLOrdinalEnumJdbcType.class, MemberCategory.values())
                .registerType(PostgreSQLStructCastingJdbcType.class, MemberCategory.values())
                .registerType(PostgreSQLStructPGObjectJdbcType.class, MemberCategory.values())
                .registerType(PostgreSQLUUIDJdbcType.class, MemberCategory.values());
    }
}
