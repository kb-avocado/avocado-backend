package com.avocado.global.config;

import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;

public class WebConfig extends AbstractAnnotationConfigDispatcherServletInitializer {

    /**
     * 루트 ApplicationContext 설정을 등록한다.
     *
     * @return 루트 설정 클래스 목록
     */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{
                RootConfig.class,
                SecurityConfig.class
        };
    }

    /**
     * DispatcherServlet ApplicationContext 설정을 등록한다.
     *
     * @return Servlet 설정 클래스 목록
     */
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{
                ServletConfig.class,
                SwaggerConfig.class
        };
    }

    /**
     * DispatcherServlet이 처리할 기본 경로를 설정한다.
     *
     * @return Servlet 매핑 경로
     */
    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    /**
     * 요청과 응답의 문자 인코딩을 UTF-8로 설정한다.
     *
     * @return Servlet Filter 목록
     */
    @Override
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.setForceEncoding(true);
        return new Filter[]{encodingFilter};
    }
}
