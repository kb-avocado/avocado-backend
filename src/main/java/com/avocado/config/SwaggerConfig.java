package com.avocado.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    // 스웨거 문서화 설정을 위한 빈 설정
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .apiInfo(apiInfo())
                .select()
                // 해당 패키지 하위의 컨트롤러만 찾아서 문서화
                .apis(RequestHandlerSelectors.basePackage("com.avocado"))
                .paths(PathSelectors.any())
                .build();
    }

    // Swagger UI 웹페이지에 접속했을 때 제목과 설명을 정의
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                // 제목
                .title("Avocado API")
                // 설명
                .description("아보카도 백엔드 API 명세서")
                // API 버전
                .version("1.0.0")
                .build();
    }
}
