package ir.amv.snippets.sortable;

import ir.amv.snippets.sortable.finder.LuceneProductFinder;
import ir.amv.snippets.sortable.finder.ProductFinder;
import ir.amv.snippets.sortable.json.GsonUtil;
import ir.amv.snippets.sortable.model.Listing;
import ir.amv.snippets.sortable.model.Product;
import ir.amv.snippets.sortable.model.Result;

import java.io.*;
import java.util.*;

/**
 * Created by AMV on 5/1/2016.
 */
public class Runner {

    public static void main(String[] args) {
        try {
            System.out.println("Arguments: [ListingFileName] [ProductsFileName] [OutputFileName]");
            String listingFileName;
            String productsFileName;
            String outputFileName;
            if (args.length > 0) {
                listingFileName = args[0];
            } else {
                listingFileName = "data/listings.txt";
            }
            if (args.length > 1) {
                productsFileName = args[1];
            } else {
                productsFileName = "data/products.txt";
            }
            if (args.length > 2) {
                outputFileName = args[2];
            } else {
                outputFileName = "result.json";
            }
            List<Listing> listings = GsonUtil.readFromFile(listingFileName, Listing.class);
            List<Product> products = GsonUtil.readFromFile(productsFileName, Product.class);

            List<Result> resultsList = new ArrayList<Result>();
            List<Listing> notMatched = new ArrayList<Listing>();

            LuceneProductFinder luceneProductFinder = new LuceneProductFinder(products, listings);
            System.out.println("new Date() = " + new Date());

            for (int i = 0; i < listings.size(); i++) {
                Listing listing = listings.get(i);
                long currentTimeMillis = System.currentTimeMillis();
                Product luceneProduct = luceneProductFinder.findProduct(listing, i);
                if (luceneProduct != null) {
                    String productName = luceneProduct.getProductName();
                    Result result = null;
                    for (Result stringListMap : resultsList) {
                        if (stringListMap.getProductName().equals(productName)) {
                            result = stringListMap;
                        }
                    }
                    if (result == null) {
                        result = new Result();
                        result.setProductName(productName);
                        result.setListings(new ArrayList<Listing>());
                        resultsList.add(result);
                    }
                    result.getListings().add(listing);
                } else {
                    notMatched.add(listing);
                }
                if (i % 1000 == 0) {
                    System.out.println("Handled " + i + " listings");
                }
            }
            System.out.println("new Date() = " + new Date());
            GsonUtil.toJson(resultsList, new FileOutputStream(outputFileName));
            GsonUtil.toJson(notMatched, new FileOutputStream("NotMatched.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateCSV(List<Object[]> data, FileOutputStream fileOutputStream, String header) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
        writer.write(header);
        writer.newLine();
        int i = -1;
        for (Object[] objects : data) {
            i++;
            StringJoiner stringJoiner = new StringJoiner(",");
            for (Object object : objects) {
                stringJoiner.add(String.valueOf(object).replaceAll(",", "_"));
            }
            writer.write(stringJoiner.toString());
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }
}
