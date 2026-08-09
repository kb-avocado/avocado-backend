package com.avocado.global.config;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
// 트랜잭션 관리 활성화
@EnableTransactionManagement
// 매퍼 등록
@MapperScan({
        "com.avocado.domain.account.mapper",
        "com.avocado.domain.merchant.mapper",
        "com.avocado.domain.user.mapper",
        "com.avocado.domain.notification.mapper",
        "com.avocado.domain.wallet.mapper",
        "com.avocado.domain.transaction.mapper",
        "com.avocado.domain.news.mapper",
        "com.avocado.domain.piggybank.mapper",
        "com.avocado.domain.report.mapper",
        "com.avocado.domain.transfer.mapper",
        "com.avocado.domain.family.mapper",
})
public class MyBatisConfig {

    // MyBatis가 사용할 SqlSessionFactory를 등록
    @Bean
    public SqlSessionFactory sqlSessionFactory(
            DataSource dataSource,
            ApplicationContext applicationContext
    ) throws Exception {
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();

        // MyBatis가 DB 커넥션을 얻을 DataSource 지정
        sessionFactoryBean.setDataSource(dataSource);

        // MyBatis 전역 설정 파일 지정
        sessionFactoryBean.setConfigLocation(
                applicationContext.getResource("classpath:mybatis/mybatis-config.xml")
        );

        // mapper 디렉터리 하위 .xml 파일 등록
        sessionFactoryBean.setMapperLocations(
                applicationContext.getResources(
                        "classpath:mapper/**/*.xml"
                )
        );

        return sessionFactoryBean.getObject();
    }

    // DataSource 기반의 트랜잭션 매니저를 등록
    @Bean
    public PlatformTransactionManager transactionManager(
            DataSource dataSource
    ) {
        return new DataSourceTransactionManager(dataSource);
    }
}
