package com.avocado.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * 운영 환경에서 WAR 외부의 설정 파일을 등록한다.
 */
@Configuration
@Profile("prod")
@PropertySource("file:${AVOCADO_CONFIG_PATH}")
public class ProdPropertyConfig {
}
