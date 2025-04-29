package com.metaorta.kaspi.service;

import com.metaorta.kaspi.model.Proxy;
import com.metaorta.kaspi.repository.ProxyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class ProxyService {

    private final ProxyRepository proxyRepository;

    @Value("proxy.username")
    public static String PROXY_USERNAME;

    @Value("proxy.password")
    public static String PROXY_PASSWORD;

    @Autowired
    public ProxyService(ProxyRepository proxyRepository) {
        this.proxyRepository = proxyRepository;
    }

    public BlockingQueue<Proxy> getProxyQueueByMerchantId(int merchantId) {
        List<Proxy> proxyList = proxyRepository.findByMerchantId(merchantId);
        return new LinkedBlockingQueue<>(proxyList);
    }
}
