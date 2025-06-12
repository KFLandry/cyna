package com.cyna.products.dto;

import com.cyna.products.models.PricingModel;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PriceDto {
    private String priceId;

    @SerializedName("currency")
    String currency;

    @SerializedName("amount")
    Long amount;

    @SerializedName("productId")
    long productId;

    @SerializedName("ProductName")
    String productName;

    @SerializedName("description")
    private String description;

    @SerializedName("pricingModel")
    private PricingModel pricingModel;
}
