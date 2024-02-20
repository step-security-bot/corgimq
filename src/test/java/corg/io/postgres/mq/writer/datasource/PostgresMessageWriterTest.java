package corg.io.postgres.mq.writer.datasource;

import corg.io.postgres.mq.RelationalTestUtil;
import corg.io.postgres.mq.writer.MessageWriterTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.SQLException;

@Testcontainers(disabledWithoutDocker = true)
public class PostgresMessageWriterTest extends MessageWriterTest {
    private static JdbcDatabaseContainer<?> postgres;

    @BeforeAll
    static void setupPostgres() {
        postgres = RelationalTestUtil.postgresContainer();
        postgres.start();
    }

    @Override
    protected String getUserName() {
        return RelationalTestUtil.postgresUserName(postgres);
    }

    @Override
    protected String getPassword() {
        return RelationalTestUtil.postgresPassword(postgres);
    }

    @Override
    protected String getJdbcUrl() {
        return postgres.getJdbcUrl();
    }

    @AfterEach
    public void testTearDown() throws SQLException {
        try(var conn = getConnection()) {
            RelationalTestUtil.cleanupPostgres(conn, messageQueue.tableSchemaName());
        }
    }

    @AfterAll
    static void shutdownPostgres() {
        postgres.close();
    }
}
