package io.github.tiagoiwamoto.springjpa.config;

import io.github.tiagoiwamoto.springjpa.entity.Jogo;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import java.util.List;

public class NativeRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // JDBC / Dialect
        hints.resources().registerType(org.hibernate.dialect.PostgreSQLDialect.class);
        hints.reflection().registerType(
                org.hibernate.dialect.PostgreSQLDialect.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS
        );

        // Alguns tipos do Hibernate usados via service/strategy
        List<Class<?>> hibernateTypes = List.of(
                org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl.class,
                org.hibernate.engine.jdbc.env.spi.JdbcEnvironment.class,
                org.hibernate.boot.model.relational.ColumnOrderingStrategyStandard.class
        );
        for (Class<?> t : hibernateTypes) {
            hints.reflection().registerType(
                    t,
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS
            );
        }

        // Registrar proxy/hint do Hibernate
        hints.reflection().registerType(Hibernate.class, MemberCategory.INVOKE_PUBLIC_METHODS);
        hints.reflection().registerType(HibernateProxy.class, MemberCategory.INVOKE_PUBLIC_METHODS);

        // Registrar entidades e inner classes (ex.: builders)
        registerAnnotatedTypes(hints, "io.github.tiagoiwamoto.springjpa.entity", Jogo.class);
        // registro explícito do builder interno se existir
        try {
            Class<?> builder = Class.forName("io.github.tiagoiwamoto.springjpa.entity.Jogo$JogoBuilder");
            hints.reflection().registerType(
                    builder,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.ACCESS_DECLARED_FIELDS,
                    MemberCategory.INVOKE_PUBLIC_METHODS
            );
        } catch (ClassNotFoundException ignored) {}
    }

    private void registerAnnotatedTypes(RuntimeHints hints, String basePackage, Class<?> annotation) {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
            try {
                Class<?> type = Class.forName(bd.getBeanClassName());
                hints.reflection().registerType(
                        type,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_METHODS,
                        MemberCategory.ACCESS_DECLARED_FIELDS,
                        MemberCategory.INVOKE_PUBLIC_METHODS
                );
            } catch (ClassNotFoundException ignored) {}
        }
    }
}
