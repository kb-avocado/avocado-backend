package com.avocado.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
class RootConfigTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("HikariCP DataSource 빈 생성 및 DB 연결 테스트")
    public void testDataSourceConnection() throws Exception {
        assertThat(dataSource)
                .as("DataSource 빈이 생성 확인")
                .isNotNull();

        assertThat(dataSource)
                .as("HikariDataSource 인스턴스 확인")
                .isInstanceOf(HikariDataSource.class);

        try (Connection conn = dataSource.getConnection()) {
            assertThat(conn)
                    .as("DB 커넥션 획득 확인")
                    .isNotNull();

            log.info(
                    "DB 커넥션 성공 - URL: {}, 사용자: {}, DB: {}, 버전: {}",
                    conn.getMetaData().getURL(),
                    conn.getMetaData().getUserName(),
                    conn.getMetaData().getDatabaseProductName(),
                    conn.getMetaData().getDatabaseProductVersion()
            );
        }
    }


}