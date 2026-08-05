package com.avocado.config;

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
        "com.avocado.account.mapper",
        "com.avocado.merchant.mapper",
        "com.avocado.user.mapper",
        "com.avocado.notification.mapper",
        "com.avocado.wallet.mapper",
        "com.avocado.transaction.mapper",
        "com.avocado.news.mapper",
        "com.avocado.piggybank.mapper",
        "com.avocado.report.mapper",
        "com.avocado.transfer.mapper",
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
