package com.metaorta.kaspi.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.metaorta.kaspi.model.Merchant;
import com.metaorta.kaspi.repository.MerchantRepository;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MerchantService {
    private final MerchantRepository merchantRepository;

    private final CloseableHttpClient httpClient;

    @Autowired
    public MerchantService(MerchantRepository merchantRepository, CloseableHttpClient httpClient) {
        this.merchantRepository = merchantRepository;
        this.httpClient = httpClient;
    }

    public Optional<Merchant> getMerchantById(int id) {
        return merchantRepository.findById(id);
    }

    public void save(Merchant merchant) {
        Optional<Merchant> foundMerchant = merchantRepository.findByMerchantId(merchant.getMerchantId());
        foundMerchant.ifPresent(value -> merchant.setId(value.getId()));
        merchantRepository.save(merchant);
    }

    public String getCurrentPriceListNumber(String merchantId, String sessionId) {
        HttpGet request = new HttpGet("https://mc.shop.kaspi.kz/pricefeed/upload/merchant/files/last?m=" + merchantId);
        request.addHeader("Cookie", "X-Mc-Api-Session-Id=" + sessionId + ";");
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        request.addHeader("User-Agent", "PostmanRuntime/7.42.0");
        request.addHeader("Accept", "*/*");
        request.addHeader("Accept-Encoding", "gzip, deflate, br");
        request.addHeader("Connection", "keep-alive");

        try (CloseableHttpResponse response = httpClient.execute(request)){
            String json = EntityUtils.toString(response.getEntity());

            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            return jsonObject.get("id").getAsString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
