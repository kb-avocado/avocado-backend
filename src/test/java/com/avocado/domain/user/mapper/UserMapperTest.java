package com.avocado.domain.user.mapper;

import com.avocado.global.config.DataSourceConfig;
import com.avocado.global.config.LocalPropertyConfig;
import com.avocado.global.config.MyBatisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = {
        LocalPropertyConfig.class,
        DataSourceConfig.class,
        MyBatisConfig.class
})
@Transactional
class UserMapperTest {

}