package com.avocado.global.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        RootConfig.class,
        SecurityConfig.class
})
class RootConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 실제 루트 ApplicationContext가 정상적으로 생성되는지 확인한다.
     */
    @Test
    @DisplayName("Root ApplicationContext 정상 로딩 테스트")
    void rootContextLoads() {
        assertThat(applicationContext)
                .as("ApplicationContext 생성 확인")
                .isNotNull();

        assertThat(dataSource)
                .as("DataSource 빈 등록 확인")
                .isNotNull();

        assertThat(passwordEncoder)
                .as("PasswordEncoder 빈 등록 확인")
                .isNotNull();
    }

    /**
     * RootConfig에 등록된 HikariCP DataSource로 실제 DB 연결을 확인한다.
     */
    @Test
    @DisplayName("HikariCP DataSource 빈 생성 및 DB 연결 테스트")
    void testDataSourceConnection() throws Exception {
        assertThat(dataSource)
                .as("DataSource 빈 생성 확인")
                .isInstanceOf(HikariDataSource.class);

        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection)
                    .as("DB 커넥션 획득 확인")
                    .isNotNull();

            log.info(
                    "DB 커넥션 성공 - URL: {}, 사용자: {}, DB: {}, 버전: {}",
                    connection.getMetaData().getURL(),
                    connection.getMetaData().getUserName(),
                    connection.getMetaData().getDatabaseProductName(),
                    connection.getMetaData().getDatabaseProductVersion()
            );
        }
    }
}