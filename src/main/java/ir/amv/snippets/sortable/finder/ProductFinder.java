package ir.amv.snippets.sortable.finder;

import ir.amv.snippets.sortable.finder.matcher.IProductMatcher;
import ir.amv.snippets.sortable.finder.matcher.MatchResult;
import ir.amv.snippets.sortable.finder.matcher.impl.WeightedMatcher;
import ir.amv.snippets.sortable.json.GsonUtil;
import ir.amv.snippets.sortable.model.Listing;
import ir.amv.snippets.sortable.model.Product;

import java.util.*;

/**
 * Created by AMV on 5/1/2016.
 */
public class ProductFinder {

    public static final double THRESHOLD = 0.75;
    private List<Product> products;

    public ProductFinder(List<Product> products) {
        this.products = products;
    }

    public Product findProduct(Listing listing) {
        IProductMatcher matcher = new WeightedMatcher();
        Map<Product, Double> matchedProducts = new HashMap<Product, Double>();
        for (Product product : products) {
            MatchResult matchResult = matcher.match(product, listing);
            if (matchResult.getResemblanceScore() > THRESHOLD) {
                matchedProducts.put(product, matchResult.getResemblanceScore());
            }
        }
        if (matchedProducts.size() == 1) {
            return matchedProducts.keySet().iterator().next();
        }
        if (matchedProducts.size() > 1) {
            Collection<Double> resemblanceScores = new ArrayList<Double>(matchedProducts.values());
            Double max = Collections.max(resemblanceScores);
            resemblanceScores.remove(max);
            Double minDiff = Double.MAX_VALUE;
            for (Double resemblanceScore : resemblanceScores) {
                double diff = max - resemblanceScore;
                if (diff < minDiff) {
                    minDiff = diff;
                }
            }
            if (minDiff > 0.2) {
                for (Product product : matchedProducts.keySet()) {
                    if (matchedProducts.get(product) == max) {
                        return product;
                    }
                }
            }
//            System.out.println("{\"listing\":" + listing + ", \"matched\":" + matchedProducts.keySet());
            return null;
        }
        return null;
    }
}
