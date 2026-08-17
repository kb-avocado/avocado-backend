package com.avocado.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebMvc
@ComponentScan(
        basePackages = {
                "com.avocado.domain",
                "com.avocado.global.exception"
        },
        includeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = Controller.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = ControllerAdvice.class
                )
        },
        useDefaultFilters = false
)
public class ServletConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * 프론트엔드 서버와 통신하기 위한 CORS 정책을 설정한다.
     *
     * @param registry CORS 설정 Registry
     */
    @Override
    public void addCorsMappings(
            CorsRegistry registry
    ) {
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .toArray(String[]::new);

        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * Jackson에 Java Time 직렬화 설정을 추가한다.
     *
     * @param converters HTTP MessageConverter 목록
     */
    @Override
    public void extendMessageConverters(
            List<HttpMessageConverter<?>> converters
    ) {
        for (HttpMessageConverter<?> converter : converters) {
            if (!(converter instanceof MappingJackson2HttpMessageConverter)) {
                continue;
            }

            ObjectMapper objectMapper = ((MappingJackson2HttpMessageConverter) converter)
                    .getObjectMapper();

            // java.time 타입 지원. 이미 등록돼 있으면 무시된다.
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
    }

    /**
     * 정적 리소스와 Swagger UI 리소스 경로를 등록한다.
     *
     * @param registry ResourceHandler Registry
     */
    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry
    ) {
        // 정적 자원 경로 매핑
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/resources/");

        // Swagger UI HTML 화면 리소스 매핑
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/");

        // Swagger가 사용하는 JS, CSS 등 정적 파일 매핑
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
