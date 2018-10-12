package com.ibagroup.workfusion.rpa.systems.googlesearch.to;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProductSummaryTO {

    @SerializedName("product_link")
    @Expose
    private String productLink;

    @SerializedName("product_summary")
    @Expose
    private String productSummary;

    public String getProductLink() {
        return productLink;
    }

    public void setProductLink(String productLink) {
        this.productLink = productLink;
    }

    public String getProductSummary() {
        return productSummary;
    }

    public void setProductSummary(String productSummary) {
        this.productSummary = productSummary;
    }

}
