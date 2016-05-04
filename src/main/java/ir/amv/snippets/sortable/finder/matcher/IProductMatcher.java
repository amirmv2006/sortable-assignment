package ir.amv.snippets.sortable.finder.matcher;

import ir.amv.snippets.sortable.model.Listing;
import ir.amv.snippets.sortable.model.Product;

/**
 * Created by AMV on 5/1/2016.
 */
public interface IProductMatcher {

    MatchResult match(Product product, Listing listing);
}
