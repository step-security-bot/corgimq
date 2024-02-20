package corg.io.postgres.mq.table.datasource;

import corg.io.postgres.mq.RelationalTestUtil;
import corg.io.postgres.mq.table.MessageQueueTableTestHarness;
import org.junit.jupiter.api.AfterEach;

import java.sql.SQLException;

public class H2MessageQueueTableTest extends MessageQueueTableTestHarness {
    @Override
    protected String getUserName() {
        return RelationalTestUtil.h2UserName();
    }

    @Override
    protected String getPassword() {
        return RelationalTestUtil.getH2Password();
    }


    @Override
    protected String getJdbcUrl() {
        return RelationalTestUtil.h2JdbcUrl();
    }

    @AfterEach
    public void testTearDown() throws SQLException {
        try(var conn = getConnection()) {
            RelationalTestUtil.cleanupH2(conn);
        }
    }
}
