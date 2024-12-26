package com.metaorta.kaspi.service;


import com.metaorta.kaspi.dto.ProductDTO;
import com.metaorta.kaspi.exception.SessionExpiredException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final CloseableHttpClient httpClient;

    List<ProductDTO> products = new ArrayList<>();

    private final UserSessionService userSessionService;


    public ProductService(UserSessionService userSessionService) {
        this.userSessionService = userSessionService;
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
            if(response.getCode() == 401 || response.getCode() == 403){
                throw new SessionExpiredException("Session expired");
            }
            System.out.println(responseBody);
            JSONObject jsonResponse = new JSONObject(responseBody);
            return jsonResponse.optInt("published", 0);
        }catch (SessionExpiredException e) {
            //TODO: Take new session and start it again
            throw new RuntimeException("Error while fetching offer count", e);
        }
        catch (Exception e) {

            throw new RuntimeException("Error while fetching offer count", e);
        }
    }


    public String getNewSession(String username,String password){
        String url = "http://localhost:8081/getSession";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost postRequest = new HttpPost(url);

            postRequest.setHeader("Content-Type", "application/json");
            postRequest.setHeader("User-Agent", "insomnium/0.2.3");

            String jsonInput = "{\n" +
                    "  \"email\": \"" + username + "\",\n" +
                    "  \"password\": \"" + password + "\"\n" +
                    "}";

            postRequest.setEntity(new StringEntity(jsonInput));

            // Отправляем запрос
            CloseableHttpResponse response = client.execute(postRequest);

            // Читаем и возвращаем ответ
            int statusCode = response.getCode();
            if (statusCode == 200) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject jsonResponse = new JSONObject(responseBody);
                String sessionId = jsonResponse.optString("sid");
                userSessionService.saveSessionToRedis(username,sessionId);
                return sessionId;

            } else {
                return "Error: " + statusCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred: " + e.getMessage();
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


