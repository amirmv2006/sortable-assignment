package ir.amv.snippets.sortable.model;

import ir.amv.snippets.sortable.json.GsonUtil;

/**
 * Created by AMV on 5/1/2016.
 */
public class Listing {

    private String title;
    private String manufacturer;
    private String currency;
    private String price;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return GsonUtil.gson().toJson(this);
    }
}
