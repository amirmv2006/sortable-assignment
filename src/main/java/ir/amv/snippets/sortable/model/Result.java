package ir.amv.snippets.sortable.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by AMV on 5/1/2016.
 */
public class Result {

    @SerializedName("product_name")
    @JsonProperty("product_name")
    private String productName;
    private List<Listing> listings;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public List<Listing> getListings() {
        return listings;
    }

    public void setListings(List<Listing> listings) {
        this.listings = listings;
    }
}
