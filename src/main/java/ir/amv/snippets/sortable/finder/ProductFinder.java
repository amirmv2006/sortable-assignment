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

    /**
     * finds the Product matching the listing
     * @param listing
     * @return
     */
    public Product findProduct(Listing listing) {
        IProductMatcher matcher = new WeightedMatcher();
        Map<Product, Double> matchedProducts = new HashMap<Product, Double>();
        for (Product product : products) {
            MatchResult matchResult = matcher.match(product, listing);
            if (matchResult.getResemblanceScore() > THRESHOLD) {
                matchedProducts.put(product, matchResult.getResemblanceScore());
            }
        }
        if (matchedProducts.size() == 1) {  // if only one product is found, return it
            return matchedProducts.keySet().iterator().next();
        }
        if (matchedProducts.size() > 1) {   // if more than one product is found,
                                            // return the product with max score if there is no other product with a close score
            Collection<Double> resemblanceScores = new ArrayList<Double>(matchedProducts.values());
            Double max = Collections.max(resemblanceScores); // get the max score
            resemblanceScores.remove(max); // remove from the scores to find the minimum difference
            Double minDiff = Double.MAX_VALUE;
            for (Double resemblanceScore : resemblanceScores) {
                double diff = max - resemblanceScore;
                if (diff < minDiff) {
                    minDiff = diff;
                }
            }
            if (minDiff > 0.2) {// if minimum difference is more than 0.2, we can return the max score
                for (Product product : matchedProducts.keySet()) {
                    if (matchedProducts.get(product) == max) {
                        return product;
                    }
                }
            }
//            System.out.println("{\"listing\":" + listing + ", \"matched\":" + matchedProducts.keySet());
            return null; // when there are two products with very close scores, return none of them!
        }
        return null;
    }
}
