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

    /**
     * this method will return a MatchResult after comparing a Product and a Listing. a resemblanceScore will be calculated
     * for the match based on the properties that match and how they are matched. the matching will calculate the resemblanceScore
     * for 4 properties and then return the weightedAverage for these 4 attributes, these for attributes and their weights are:
     *      name        => 10
     *      family      => 40
     *      model       => 30
     *      manufacturer=> 15
     * these weights have been calculated based on experience, like the sample data for AI algorithms.
     * @param product
     * @param listing
     * @return MatchResult containing resemblanceScore
     */
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

    /**
     * Calculates the resemblanceScore between two attributes' value. first it splits the strings by the characters ' -_' and
     * then foreach token,
     * if it finds exact match will give score=1,
     * if one of the tokens is starts with the other one, score=0.5
     * regardless of these two, if there is the same number on both tokens the score will be increased by 1, and if there is
     * a number on both strings, but doesn't match, the score will be decreased by 1. (if we have the same number on the tokens,
     * they are most likely the same, and we have different numbers they are most likely different)
     * So, the score for each token may be more than 1...
     * then the sum of scores for each token will be divided to number of tokens on product attribute
     * @param productAttribute
     * @param listingAttribute
     * @return
     */
    private double getResemblanceScore(String productAttribute, String listingAttribute) {
        ArrayList<String> listingTitleTokens = tokenize(listingAttribute);
        ArrayList<String> productNameTokens = tokenize(productAttribute);
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
                Matcher listingTitleMatcher = compile.matcher(listingAttribute);
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
