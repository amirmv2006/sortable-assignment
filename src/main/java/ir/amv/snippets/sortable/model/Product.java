package ir.amv.snippets.sortable.model;

import com.google.gson.annotations.SerializedName;
import ir.amv.snippets.sortable.json.GsonUtil;

/**
 * Created by AMV on 5/1/2016.
 */
public class Product {

    @SerializedName("product_name")
    private String productName;
    private String manufacturer;
    private String model;
    private String family;
    @SerializedName("announced-date")
    private String announcedDate;

    public Product() {
    }

    public Product(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getAnnouncedDate() {
        return announcedDate;
    }

    public void setAnnouncedDate(String announcedDate) {
        this.announcedDate = announcedDate;
    }

    @Override
    public String toString() {
        return GsonUtil.gson().toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;

        return productName.equals(product.productName);

    }

    @Override
    public int hashCode() {
        return productName.hashCode();
    }
}
