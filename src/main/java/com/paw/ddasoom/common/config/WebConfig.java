package com.paw.ddasoom.common.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.paw.ddasoom.animal.util.AnimalGenderConverter;
import com.paw.ddasoom.animal.util.AnimalKindConverter;

@Configuration
public class WebConfig implements WebMvcConfigurer{

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

    // RestClient에 접속 시 5초 이상, 읽기 시 300초 이상 걸리면 요청 스레드가 대기하지 않음
    ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
        .withConnectTimeout(Duration.ofSeconds(5))
        .withReadTimeout(Duration.ofSeconds(300));
    ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect().build(settings);

    return RestClient.builder()
        .uriBuilderFactory(factory)     // baseUrl을 아는 팩토리
        .requestFactory(requestFactory) // requestFactory
        .build();                       // 여기선 .baseUrl() 중복 호출 불필요
    }

    // from() 메서드가 있으니 프론트에서 한글("개", "고양이")로 보내는 구조.
    // Spring의 기본 enum 변환은 from()을 모르고 상수명(D, C)으로만 변환하기 때문에 커스텀 컨버터가 필요합니다.
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new AnimalKindConverter());
        registry.addConverter(new AnimalGenderConverter());
    }
}
