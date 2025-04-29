package com.metaorta.kaspi.service.session;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserSessionService {
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public UserSessionService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getUserSessionIdFromRedis(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void saveSessionToRedis(String username, String sessionId) {
        redisTemplate.opsForValue().set(username, sessionId);
    }

    public String getNewSession(String username,String password) {
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

            CloseableHttpResponse response = client.execute(postRequest);

            int statusCode = response.getCode();

            if (statusCode == 200) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject jsonResponse = new JSONObject(responseBody);
                String sessionId = jsonResponse.optString("sid");
                saveSessionToRedis(username,sessionId);
                return sessionId;
            } else {
                return "Error: " + statusCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred: " + e.getMessage();
        }
    }
}
