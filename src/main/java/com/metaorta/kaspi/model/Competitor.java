package com.metaorta.kaspi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Competitor {
    private String merchantSku;

    private Integer price;
}
