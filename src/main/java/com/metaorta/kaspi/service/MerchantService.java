package com.metaorta.kaspi.service;

import com.metaorta.kaspi.model.Merchant;
import com.metaorta.kaspi.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;

    @Autowired
    public MerchantService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    public Optional<Merchant> getMerchantById(int id) {
        return merchantRepository.findById(id);
    }
}
