package com.metaorta.kaspi;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.ui.ModelMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class KaspiApplication {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public CloseableHttpClient httpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofDays(5000))
                .setResponseTimeout(Timeout.ofDays(5000))// Connection timeout (in ms)
                .setConnectionRequestTimeout(Timeout.ofDays(5000))
                .setConnectionKeepAlive(TimeValue.MAX_VALUE)
                .setExpectContinueEnabled(true)
                .setDefaultKeepAlive(5000, TimeUnit.DAYS)
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1000000);
        factory.setReadTimeout(30000000);

        RestTemplate restTemplate = new RestTemplate(factory);

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                // Log the error or handle specific HTTP status codes
                if (response.getStatusCode().is5xxServerError()) {
                    throw new HttpServerErrorException(response.getStatusCode());
                } else if (response.getStatusCode().is4xxClientError()) {
                    throw new HttpClientErrorException(response.getStatusCode());
                }
            }
        });

        return restTemplate;
    }


    public static void main(String[] args) {
        SpringApplication.run(KaspiApplication.class, args);
    }
}
