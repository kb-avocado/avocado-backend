package com.avocado.global.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Controller;

import javax.sql.DataSource;

@Configuration
@ComponentScan(
        basePackages = "com.avocado.domain",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                classes = Controller.class
        )
)
@Import({
        LocalPropertyConfig.class,
        ProdPropertyConfig.class,
        DataSourceConfig.class,
        MyBatisConfig.class,
        RedisConfig.class,
        SchedulingConfig.class
})
public class RootConfig {

}
