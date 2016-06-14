package ir.amv.snippets.sortable.finder.matcher.impl;

import ir.amv.snippets.sortable.finder.matcher.IProductMatcher;
import ir.amv.snippets.sortable.finder.matcher.MatchResult;
import ir.amv.snippets.sortable.model.Listing;
import ir.amv.snippets.sortable.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by AMV on 5/1/2016.
 */
public class WeightedMatcher
        implements IProductMatcher{

    public static final double PRODUCT_NAME_WEIGHT = 10;
    public static final double PRODUCT_FAMILY_WEIGHT = 40;
    public static final double PRODUCT_MODEL_WEIGHT = 30;
    public static final double MANUFACTURER_WEIGHT = 15;

    public MatchResult match(Product product, Listing listing) {
        List<Double> resemblances = new ArrayList<Double>();
        double productNameResemblance = getResemblanceScore(product.getProductName(), listing.getTitle());
//        System.out.println("productNameResemblance = " + productNameResemblance);
        resemblances.add(productNameResemblance * PRODUCT_NAME_WEIGHT);
        if (product.getFamily() != null) {
            double familyResemblance = getResemblanceScore(product.getFamily(), listing.getTitle());
            resemblances.add(familyResemblance * PRODUCT_FAMILY_WEIGHT);
//            System.out.println("familyResemblance = " + familyResemblance);
        } else {
            resemblances.add(PRODUCT_FAMILY_WEIGHT);
        }
        if (product.getModel() != null) {
            double modelResemblance = getResemblanceScore(product.getModel(), listing.getTitle());
            resemblances.add(modelResemblance * PRODUCT_MODEL_WEIGHT);
//            System.out.println("modelResemblance = " + modelResemblance);
        } else {
            resemblances.add(PRODUCT_MODEL_WEIGHT);
        }
        double manufacturerResemblanceScore = getResemblanceScore(product.getManufacturer(), listing.getManufacturer());
//        System.out.println("manufacturerResemblanceScore = " + manufacturerResemblanceScore);
        resemblances.add(manufacturerResemblanceScore * MANUFACTURER_WEIGHT);
        double resemblanceSum = 0;
        for (Double titleMathingResemblance : resemblances) {
            resemblanceSum += titleMathingResemblance;
        }
        return new MatchResult(
                (resemblanceSum) / (PRODUCT_NAME_WEIGHT + PRODUCT_FAMILY_WEIGHT + PRODUCT_MODEL_WEIGHT + MANUFACTURER_WEIGHT));
    }

    private double getResemblanceScore(String productName, String listingTitle) {
        ArrayList<String> listingTitleTokens = tokenize(listingTitle);
        ArrayList<String> productNameTokens = tokenize(productName);
        double matchedCount = 0;
        for (String listingTitleToken : listingTitleTokens) {
            for (String productNameToken : productNameTokens) {
                boolean matched = false;
                if (productNameToken.equals(listingTitleToken)) {
                    matched = true;
                    matchedCount++;
                } else if (listingTitleToken.startsWith(productNameToken)) {
                    matched = true;
                    matchedCount+= 0.5;
                } else if (productNameToken.startsWith(listingTitleToken)) {
                    matched = true;
                    matchedCount+= 0.5;
                }
//                Integer listingTitleNumber = findNumber(listingTitle);
//                Integer prodNameNumber = findNumber(productNameToken);
//                if (matched && listingTitleNumber != null && prodNameNumber != null) {
//                    if (listingTitleNumber.equals(prodNameNumber)) {
                Pattern compile = Pattern.compile(".*?(\\d+).*");
                Matcher listingTitleMatcher = compile.matcher(listingTitle);
                Matcher productNameMatcher = compile.matcher(productNameToken);
                if (matched && listingTitleMatcher.matches() && productNameMatcher.matches()) {
                    String listingNumber = listingTitleMatcher.group(1);
                    String productNumber = productNameMatcher.group(1);
                    if (listingNumber.equals(productNumber)) {
                        matchedCount++;
                    } else {
                        matchedCount--;
                    }
                }
            }
        }
        return matchedCount / (double) productNameTokens.size();
    }

    private Integer findNumber(String listingTitle) {
        String resultStr = "";
        for (int i = 0; i < listingTitle.length(); i++) {
            char c = listingTitle.charAt(i);
            try {
                Integer integer = Integer.valueOf("" + c);
                resultStr += integer;
            } catch (NumberFormatException e) {
                if (resultStr.length() > 0) {
                    break;
                }
            }
        }
        return resultStr.equals("") ? null : Integer.valueOf(resultStr);
    }

    private ArrayList<String> tokenize(String listingTitle) {
        StringTokenizer listingTitleTokenizer = new StringTokenizer(listingTitle, " -_");
        ArrayList<String> result = new ArrayList<String>();
        while (listingTitleTokenizer.hasMoreTokens()) {
            result.add(listingTitleTokenizer.nextToken().toLowerCase());
        }
        return result;
    }

    public static void main(String[] args) {
        Product p = new Product();
        p.setProductName("Canon_IXUS_300_HS");
        p.setManufacturer("Canon");
        p.setModel("300 HS");
        p.setFamily("IXUS");

        Listing l = new Listing();
        l.setTitle("Canon PowerShot ELPH 300 HS (Black)");
        l.setManufacturer("Canon Canada");
        System.out.println(new WeightedMatcher().match(p, l).getResemblanceScore());

        p = new Product();
        p.setProductName("Canon-ELPH-300HS");
        p.setManufacturer("Canon");
        p.setModel("300 HS");
        p.setFamily("ELPH");
        System.out.println(new WeightedMatcher().match(p, l).getResemblanceScore());

        p = new Product();
        p.setProductName("Nikon_Coolpix_300");
        p.setManufacturer("Nikon");
        p.setModel("300");
        p.setFamily("Coolpix");
        System.out.println(new WeightedMatcher().match(p, l).getResemblanceScore());
    }
}
