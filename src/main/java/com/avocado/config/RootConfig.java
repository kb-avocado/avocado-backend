package com.avocado.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

@Configuration
@PropertySource("classpath:config/application-local.properties")
@Import({
        MyBatisConfig.class
})
public class RootConfig {

    @Value("${db.driver}")
    private String driverClassName;

    @Value("${db.url}")
    private String jdbcUrl;

    @Value("${db.username}")
    private String username;

    @Value("${db.password}")
    private String password;

    /**
     * MySQL 연결에 사용할 HikariCP DataSource를 생성.
     *
     * @return 구현체 HikariDataSource
     */
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        config.setDriverClassName(driverClassName);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        // 커넥션 풀 식별 이름
        config.setPoolName("AvocadoHikariPool");
        // 풀에 들어가는 최대 커넥션 수
        config.setMaximumPoolSize(10);
        // 유후 상태 커넥션 수
        config.setMinimumIdle(2);
        // 커넥션 획득 대기 최대 30초
        config.setConnectionTimeout(30_000);

        return new HikariDataSource(config);
    }
}
