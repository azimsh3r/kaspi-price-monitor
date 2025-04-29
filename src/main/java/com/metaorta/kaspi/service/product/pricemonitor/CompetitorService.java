package com.metaorta.kaspi.service.product.pricemonitor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.metaorta.kaspi.model.Competitor;
import com.metaorta.kaspi.model.Proxy;
import com.metaorta.kaspi.service.ProxyService;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class CompetitorService {
    private static final int TIMEOUT = 8000;

    private final Gson gson;
    private final BlockingQueue<Proxy> cooldownQueue = new LinkedBlockingQueue<>();

    public CompetitorService(Gson gson) {
        this.gson = gson;
    }

    public List<Competitor> getCompetitors(String sku, BlockingQueue<Proxy> proxies) throws IOException, InterruptedException {
        Map<String, Object> body = Map.of("cityId", "750000000", "limit", 5, "page", 0, "sortOption", "PRICE");
        List<Competitor> result = new ArrayList<>();

        for (int attempts = 0; attempts < 100; attempts++) {
            Proxy proxy = proxies.take();
            try (CloseableHttpClient client = createClientWithProxy(proxy)) {
                String json = gson.toJson(body);
                HttpPost request = new HttpPost("https://kaspi.kz/yml/offer-view/offers/" + sku.split("_")[0]);
                request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
                setHeaders(request);

                try (CloseableHttpResponse response = client.execute(request)) {
                    if (response.getCode() == HttpStatus.SC_OK) {
                        String jsonResponse = EntityUtils.toString(response.getEntity());
                        JsonElement offers = gson.fromJson(jsonResponse, JsonObject.class).get("offers");
                        result = gson.fromJson(offers, new TypeToken<List<Competitor>>() {}.getType());
                        break;
                    }
                }
            } catch (Exception e) {
                proxy.setCooldownStart(LocalDateTime.now());
                cooldownQueue.offer(proxy);
            } finally {
                proxies.offer(proxy);
            }
        }

        return result;
    }

    private CloseableHttpClient createClientWithProxy(Proxy proxy) {
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(proxy.getHost(), proxy.getPort()),
                new UsernamePasswordCredentials(ProxyService.PROXY_USERNAME, ProxyService.PROXY_PASSWORD.toCharArray())
        );

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(TimeValue.ofMilliseconds(TIMEOUT).toTimeout())
                .setResponseTimeout(TimeValue.ofMilliseconds(TIMEOUT).toTimeout())
                .setProxy(new HttpHost(proxy.getHost(), proxy.getPort()))
                .build();

        return HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .setDefaultRequestConfig(config)
                .build();
    }

    public void manageCooldown(BlockingQueue<Proxy> proxies) {
        List<Proxy> cooledDown = new ArrayList<>();

        cooldownQueue.removeIf(proxy -> {
            if (proxy.getCooldownStart() == null) return false;
            Duration duration = Duration.between(proxy.getCooldownStart(), LocalDateTime.now());
            if (duration.toMinutes() > 30) {
                cooledDown.add(proxy);
                return true;
            }
            return false;
        });

        proxies.addAll(cooledDown);
    }

    private void setHeaders(HttpPost request) {
        request.setHeader("Referer", "https://kaspi.kz/shop");
        request.setHeader("Content-Type", "application/json");
        request.setHeader("User-Agent", "Mozilla/5.0");
    }
}
