package com.metaorta.kaspi.service.product;

import com.metaorta.kaspi.dto.ProductDTO;
import com.metaorta.kaspi.exception.SessionExpiredException;
import com.metaorta.kaspi.service.session.UserSessionService;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductFetchService {
    private final CloseableHttpClient httpClient;
    private final UserSessionService userSessionService;

    @Autowired
    public ProductFetchService(CloseableHttpClient httpClient, UserSessionService userSessionService) {
        this.httpClient = httpClient;
        this.userSessionService = userSessionService;
    }

    public List<ProductDTO> getFetchAllProducts(String merchantId, String sessionId) {
        int offerCount = getOfferCount(merchantId, sessionId);
        int pageSize = 100;
        int totalPages = (int) Math.ceil(offerCount / (double) pageSize);

        List<ProductDTO> products = new ArrayList<>();

        for (int page = 0; page < totalPages; page++) {
            HttpGet request = buildProductListRequest(merchantId, sessionId, pageSize, page);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String json = EntityUtils.toString(response.getEntity());
                JSONArray data = new JSONObject(json).getJSONArray("data");

                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.getJSONObject(i);
                    products.add(new ProductDTO(
                            obj.optString("sku", ""),
                            obj.optString("model", ""),
                            obj.optInt("price", 0)));
                }
            } catch (Exception e) {
                throw new RuntimeException("Error fetching products", e);
            }
        }
        return products;
    }

    public int getOfferCount(String merchantId, String sessionId) {
        HttpGet request = new HttpGet("https://mc.shop.kaspi.kz/offers/api/v1/offer/count?m=" + merchantId);
        request.addHeader("cookie", "mc-sid=" + sessionId + ";");
        request.addHeader("accept", "application/json, text/plain, */*");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() == 401 || response.getCode() == 403) {
                throw new SessionExpiredException("Session expired");
            }
            String body = EntityUtils.toString(response.getEntity());
            return new JSONObject(body).optInt("published", 0);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching offer count", e);
        }
    }

    private HttpGet buildProductListRequest(String merchantId, String sessionId, int pageSize, int page) {
        HttpGet request = new HttpGet("https://mc.shop.kaspi.kz/bff/offer-view/list?m=" + merchantId
                + "&p=" + page + "&l=" + pageSize + "&a=true&t=&c=&lowStock=false");

        request.addHeader("Cookie", "mc-sid=" + sessionId + ";");
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        request.addHeader("User-Agent", "Mozilla/5.0 ...");
        request.addHeader("Accept", "*/*");

        return request;
    }
}
