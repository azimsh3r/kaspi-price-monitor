package com.metaorta.kaspi;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class KaspiApplication {

    @Bean
    public CloseableHttpClient httpClient() {
        return HttpClients.createDefault();
    }

    public static void main(String[] args) {
        SpringApplication.run(KaspiApplication.class, args);
    }

}
