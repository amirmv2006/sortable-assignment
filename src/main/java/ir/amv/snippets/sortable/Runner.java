package ir.amv.snippets.sortable;

import ir.amv.snippets.sortable.finder.ProductFinder;
import ir.amv.snippets.sortable.json.GsonUtil;
import ir.amv.snippets.sortable.model.Listing;
import ir.amv.snippets.sortable.model.Product;
import ir.amv.snippets.sortable.model.Result;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by AMV on 5/1/2016.
 */
public class Runner {

    public static void main(String[] args) {
        try {
            List<Listing> listings = GsonUtil.readFromFile("data/listings.txt", Listing.class);
            List<Product> products = GsonUtil.readFromFile("data/products.txt", Product.class);
            Map<String, Result> resultsMap = new HashMap<String, Result>();
            List<Listing> notMatched = new ArrayList<Listing>();
            ProductFinder finder = new ProductFinder(products);
            for (Listing listing : listings) {
                Product product = finder.findProduct(listing);
                if (product != null) {
                    String productName = product.getProductName();
                    Result result = resultsMap.get(productName);
                    if (result == null) {
                        result = new Result();
                        result.setProductName(productName);
                        result.setListings(new ArrayList<Listing>());
                        resultsMap.put(productName, result);
                    }
                    result.getListings().add(listing);
                } else {
                    notMatched.add(listing);
                }
            }
            GsonUtil.gson().toJson(resultsMap, new FileWriter("result.json"));
            GsonUtil.gson().toJson(notMatched, new FileWriter("NotMatched.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
