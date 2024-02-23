package corg.io.postgres.mq.model.config;

import org.immutables.value.Value;

import java.util.concurrent.TimeUnit;

@Value.Immutable
@Value.Style(
        typeAbstract = "*Abstract",
        typeImmutable = "*",
        jdkOnly = true,
        optionalAcceptNullable = true,
        strictBuilder = true
)
public interface DbConfigAbstract {
    @Value.Parameter
    String jdbcUrl();

    @Value.Parameter
    @Value.Redacted
    String username();

    @Value.Parameter
    @Value.Redacted
    String password();

    @Value.Default
    default int maxConnectionPoolSize() {
        return 10;
    }

    @Value.Default
    default long connectionMaxLifetime() {
        return TimeUnit.MINUTES.toMillis(30);
    }
}
