package com.paw.ddasoom.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.paw.ddasoom.common.security.CorsProperties;

@Configuration
public class WebConfig implements WebMvcConfigurer{

  private final CorsProperties corsProperties;

  public WebConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    /**  cors 설정 추후 수정 필요
    * @Author : 지훈
    */

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 쉼표로 구분된 문자열을 배열로 만든다.
                .allowedOrigins(corsProperties.getAllowedOrigins().split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE","OPTIONS")
                .allowedHeaders("*")
                // 쿠키나 인증 헤더와 같은 자격 증명(Credentials)을 허용할지 여부.
                // 프론트엔드와 세션/토큰을 주고받으려면 true로 설정해야 합니다.
                .allowCredentials(true)
                // 브라우저에서 preflight 요청 결과를 캐시할 시간을 초 단위로 지정합니다.
                .maxAge(3600);
    }

    /**
     * RestClient 빌드
     * @param baseUrl
     * @return restclient
     */
    @Bean
    public RestClient restClient(@Value("${api.base-url}") String baseUrl) {
    // baseUrl 끝 슬래시 보존 (서비스명 뒤에 오퍼레이션이 이어붙도록)
    String rootUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";

    // 생성자에 baseUrl을 직접 넘겨야 스킴/호스트가 유지됨
    DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(rootUrl);
    factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE); // serviceKey 이중 인코딩 방지

    return RestClient.builder()
            .uriBuilderFactory(factory)   // baseUrl을 아는 팩토리
            .build();                     // 여기선 .baseUrl() 중복 호출 불필요
}
}
