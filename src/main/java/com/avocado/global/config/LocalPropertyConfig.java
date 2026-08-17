package com.avocado.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * 로컬 개발 환경에서 사용할 설정 파일을 등록한다.
 */
@Configuration
@Profile({"default", "local"})
@PropertySource("classpath:config/application-local.properties")
public class LocalPropertyConfig {
}
