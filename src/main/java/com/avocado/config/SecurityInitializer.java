package com.avocado.config;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

/**
 * springSecurityFilterChain을 서블릿 컨테이너에 등록한다.
 * Spring Boot가 아니므로 이 클래스가 없으면 SecurityConfig를 작성해도 필터가 동작하지 않는다.
 * 필터 빈은 루트 컨텍스트(WebConfig.getRootConfigClasses)에서 찾는다.
 */
public class SecurityInitializer extends AbstractSecurityWebApplicationInitializer {
}
