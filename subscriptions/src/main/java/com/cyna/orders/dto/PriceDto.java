package com.cyna.orders.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class PriceDto {
    private String priceId;

    @SerializedName("currency")
    String currency;

    @SerializedName("amount")
    Long amount;

    @SerializedName("productId")
    String productId;

    @SerializedName("ProductName")
    String productName;

    @SerializedName("description")
    private String description;
}
