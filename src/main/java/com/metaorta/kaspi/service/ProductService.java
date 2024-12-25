package com.metaorta.kaspi.service;




import com.metaorta.kaspi.dto.ProductDTO;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final CloseableHttpClient httpClient;

    List<ProductDTO> products = new ArrayList<>();


    public ProductService() {
        this.httpClient = HttpClients.createDefault();
    }



    public Integer getOfferCount(String merchantId, String sessionId) {
        String url = "https://mc.shop.kaspi.kz/offers/api/v1/offer/count?m=" + merchantId;

        HttpGet request = new HttpGet(url);

        request.addHeader("accept", "application/json, text/plain, */*");
        request.addHeader("accept-language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
        request.addHeader("cookie", "mc-sid=" + sessionId + ";");
        request.addHeader("referer", "https://kaspi.kz/");
        request.addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println(responseBody);
            JSONObject jsonResponse = new JSONObject(responseBody);
            int count = jsonResponse.optInt("published", 0);
            return count;
        } catch (Exception e) {

            throw new RuntimeException("Error while fetching offer count", e);
        }
    }
    //TODO: Добавить метод в getFetchAllProducts , чтобы он по merchantId получал sessionId
    //TODO: Добавить Exception SessionExpired , и чтобы он отправлял запрос на получение новой сессии

    public List<ProductDTO> getFetchAllProducts(String merchantId, String sessionId) {
        List<ProductDTO> products = new ArrayList<>();
        int offerCount = getOfferCount(merchantId, sessionId);
        int pageSize = 100;
        int totalPages = (int) Math.ceil(offerCount / (double) pageSize);

        for (int page = 0; page < totalPages; page++) {
            HttpGet request = new HttpGet("https://mc.shop.kaspi.kz/bff/offer-view/list?m=" + merchantId + "&p=" + page + "&l=" + pageSize + "&a=true&t=&c=&lowStock=false");
            request.addHeader("Cookie", "mc-sid=" + sessionId + ";");
            request.addHeader("Content-Type", "application/x-www-form-urlencoded");
            request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
            request.addHeader("Accept", "*/*");
            request.addHeader("Accept-Encoding", "gzip, deflate, br");
            request.addHeader("Connection", "keep-alive");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String json = EntityUtils.toString(response.getEntity());

                JSONObject jsonResponse = new JSONObject(json);
                JSONArray data = jsonResponse.getJSONArray("data");

                for (int i = 0; i < data.length(); i++) {
                    JSONObject productJson = data.getJSONObject(i);

                    String model = productJson.optString("model", "");
                    String sku = productJson.optString("sku", "");
                    double price = productJson.optDouble("price", 0);

                    ProductDTO product = new ProductDTO(sku, model, price);
                    products.add(product);
                }

            } catch (Exception e) {
                throw new RuntimeException("Error while fetching products", e);
            }
        }

        return products;
    }

}


