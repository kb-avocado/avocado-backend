package com.avocado.global.config;

import com.avocado.global.response.ErrorResponse;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.global.security.jwt.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 시큐리티 설정. 루트 컨텍스트(WebConfig.getRootConfigClasses)에 등록되며,
 * springSecurityFilterChain의 서블릿 등록은 SecurityInitializer가 담당한다.
 * 인증 필터가 루트 컨텍스트에서 필요하므로 com.avocado.jwt 패키지를 여기서 스캔한다.
 */
@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = "com.avocado.global.security.jwt")
public class SecurityConfig {

    // 인증 없이 접근 가능한 경로
    private static final String[] PERMIT_ALL_PATHS = {
            // 로그인, 회원가입
            "/api/auth/**",
            // Swagger
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v2/api-docs",
            "/webjars/**",

            // TODO: 개발 편의용 임시 허용. 각 도메인이 인증 연동을 마치면 제거할 것
            "/api/**",              // api 전체 허용
//            "/api/home/**",         // 홈
            "/api/wallets/**",      // 전자지갑
            "/api/transfers/**",    // 송금하기
            "/api/allowances/**",   // 용돈 보내기
            "/api/transactions/**", // 거래내역
            "/api/payments/**",     // 결제&결제 환불
            "/api/piggybanks/**",   // 저금통
//            "/api/news/**",         // 뉴스
            "/api/reports/**",      // 리포트
            "/api/notifications/**",// 알림
            "/api/accounts/**",     // 계좌
            "/api/merchants/**"     // 가맹점
    };

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {
        return http
                // JWT 기반이므로 서버 세션을 만들지 않는다
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 무상태 API이고 쿠키는 SameSite로 방어하므로 CSRF 토큰은 사용하지 않는다
                .csrf(csrf -> csrf.disable())
                // 로그인/로그아웃은 직접 구현하므로 기본 제공 방식은 모두 끈다
                .formLogin(formLogin -> formLogin.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .logout(logout -> logout.disable())
                // 인증/인가 실패도 프로젝트 공통 에러 포맷으로 응답한다
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .authorizeHttpRequests(authorize -> authorize
                        // 프리플라이트(OPTIONS)를 통과시켜야 ServletConfig의 CORS 설정이 처리할 수 있다.
                        // 여기서 막으면 브라우저 요청이 CORS 단계에서 전부 실패한다.
                        .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .antMatchers(PERMIT_ALL_PATHS).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // 회원가입 시 해싱, 로그인 시 비교에 사용
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 인증되지 않은 요청 (토큰 없음, 만료, 위조)
    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) ->
                writeError(response, ErrorCode.UNAUTHORIZED);
    }

    // 인증은 됐지만 권한이 부족한 요청
    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                writeError(response, ErrorCode.FORBIDDEN);
    }

    private void writeError(
            HttpServletResponse response,
            ErrorCode errorCode
    ) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(
                response.getWriter(),
                ErrorResponse.of(errorCode)
        );
    }
}
