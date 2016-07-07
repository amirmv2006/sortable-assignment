package ir.amv.snippets.sortable.finder;

import ir.amv.snippets.sortable.lucene.SynonymAnalyzer;
import ir.amv.snippets.sortable.model.Listing;
import ir.amv.snippets.sortable.model.Product;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.CharsRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by AMV on 5/1/2016.
 */
public class LuceneProductFinder {

    public static final double THRESHOLD = 0.75;
    public static final String PRODUCT_NAME = "productName";
//    public static final String PRODUCT_NUMBERS = "productNumber";
    public static final String MODEL = "model";
    public static final String FAMILY = "family";
    public static final String MANUFACTURER = "manufacturer";
    public static final String ANNOUNCED_DATE = "announcedDate";
    public static final float PRODUCT_NAME_BOOST = 1f;
    public static final float PRODUCT_NUMBER_BOOST = 2f;
    public static final float MODEL_BOOST = 3f;
    public static final float FAMILY_BOOST = 4f;
    public static final float MANUFACTURER_BOOST = 1f;
    public static final String ORIGINAL = "Original";
    private final String productIndexPath;
    private final IndexSearcher productIndexSearcher;

    private final SynonymMap synMap;
    private final CharArraySet stopWordsSet;
//    private final String listingsIndexPath;
//    private final IndexSearcher listingsIndexSearcher;

    public LuceneProductFinder(List<Product> products, List<Listing> listings) throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        productIndexPath = tmpDir + "\\sortableIndex\\products";
        Directory directory = new RAMDirectory();
//        SimpleFSDirectory directory = new SimpleFSDirectory(Paths.get(productIndexPath));
        SynonymMap.Builder smb = new SynonymMap.Builder(true);
        smb.add(new CharsRef("hewlett"), new CharsRef("hp"), true);
        smb.add(new CharsRef("fuji"), new CharsRef("fujifilm"), true);
        smb.add(new CharsRef("agfaphoto"), new CharsRef("agfa"), true);
        synMap = smb.build();
        stopWordsSet = CharArraySet.copy(SynonymAnalyzer.STOP_WORDS_SET);
        stopWordsSet.add("canada");
        IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new SynonymAnalyzer(stopWordsSet, synMap)));
        indexProducts(writer, products);
        writer.prepareCommit();
        writer.commit();
        writer.close();

        DirectoryReader productReader = DirectoryReader.open(directory);
        productIndexSearcher = new IndexSearcher(productReader);
    }

    private void indexProducts(IndexWriter writer, List<Product> products) throws IOException {
        for (Product product : products) {
            Document doc = new Document();
            String productName = product.getProductName();
            AtomicInteger startIndex = new AtomicInteger(-1);
            AtomicInteger endIndex = new AtomicInteger(-1);
            List<String> numberParts = tryToGetModelNumber(productName, startIndex, endIndex);
            String indexedProductName = productName;
            if (numberParts != null) {
                if (product.getModel() != null) {
                    String modelReplaced = product.getModel().replaceAll("-", "").replaceAll("_", "").replaceAll(" ", "");
                    if (!numberParts.contains(modelReplaced)) {
                        numberParts.add(modelReplaced);
                    }
                }
                StringJoiner joiner = new StringJoiner(" ");
                for (String numberPart : numberParts) {
                    joiner.add(numberPart);
                }
                indexedProductName = productName.substring(0, startIndex.get()) +
                        joiner.toString() +
                        productName.substring(endIndex.get(), productName.length());
            }
            addField(doc, PRODUCT_NAME, indexedProductName, PRODUCT_NAME_BOOST);
            addField(doc, PRODUCT_NAME + ORIGINAL, productName, -1);
            List<String> modelNumberParts = tryToGetModelNumber(product.getModel(), startIndex, endIndex);
            String indexedModel = product.getModel();
            if (modelNumberParts != null) {
                StringJoiner joiner = new StringJoiner(" ");
                for (String numberPart : modelNumberParts) {
                    joiner.add(numberPart);
                }
                indexedModel = indexedModel.substring(0, startIndex.get()) +
                        joiner.toString() +
                        indexedModel.substring(endIndex.get(), indexedModel.length());
            }
            addField(doc, MODEL, indexedModel, MODEL_BOOST);
            addField(doc, MODEL + ORIGINAL, product.getModel(), -1);
            addField(doc, FAMILY, product.getFamily(), FAMILY_BOOST);
            addField(doc, FAMILY + ORIGINAL, product.getFamily(), -1);
            addField(doc, MANUFACTURER, replaceCharsInManufacturer(product.getManufacturer()), MANUFACTURER_BOOST);
            addField(doc, MANUFACTURER+ ORIGINAL, product.getManufacturer(), -1);
            addField(doc, ANNOUNCED_DATE, product.getAnnouncedDate(), -1);
            writer.addDocument(doc);
        }
    }

    private IndexSearcher indexListing(Listing listing) throws IOException {
        Directory listingDirectory = new RAMDirectory();
        IndexWriter listingWriter = new IndexWriter(listingDirectory, new IndexWriterConfig(new SynonymAnalyzer(stopWordsSet, synMap)));

        Document doc = new Document();
        String title = listing.getTitle();
        if (title.toLowerCase().contains("for")) {
            title = title.substring(0, title.toLowerCase().indexOf("for"));
        }
        AtomicInteger startIndex = new AtomicInteger(-1);
        AtomicInteger endIndex = new AtomicInteger(-1);
        List<String> numberParts = tryToGetModelNumber(title, startIndex, endIndex);
        String indexedProductName = title;
        if (numberParts != null) {
//            addField(doc, PRODUCT_NUMBERS, numberParts[0], 2f);
            StringJoiner joiner = new StringJoiner(" ");
            for (String numberPart : numberParts) {
                joiner.add(numberPart);
            }
            indexedProductName = title.substring(0, startIndex.get()) +
                    joiner.toString() +
                    title.substring(endIndex.get(), title.length());
        }
        addField(doc, PRODUCT_NAME, indexedProductName, 1f);
        addField(doc, replaceCharsInManufacturer(MANUFACTURER), listing.getManufacturer(), 1.5f);
        listingWriter.addDocument(doc);
        listingWriter.prepareCommit();
        listingWriter.commit();
        listingWriter.close();
        DirectoryReader listingReader = DirectoryReader.open(listingDirectory);
        IndexSearcher listingsIndexSearcher = new IndexSearcher(listingReader);
        return listingsIndexSearcher;
    }

    private List<String> tryToGetModelNumber(String productName, AtomicInteger startIndexResult, AtomicInteger endIndexResult) {
        productName = replaceDashUnderscore(productName.trim());
        Pattern compile = Pattern.compile(".*?(\\d+).*");
        Matcher matcher = compile.matcher(productName);
        if (matcher.matches()) {
            String numberPart = matcher.group(1);
            String numberPrefix = null;
            String numberPostfix = null;
            int startIndex = matcher.start(1);
            startIndexResult.set(startIndex);
            int endIndex = matcher.end(1);
            endIndexResult.set(endIndex);

            while (startIndex > 0 && productName.charAt(startIndex - 1) == ' ') {
                startIndex--;
            }
            for (int i = startIndex - 1; i >= -1; i--) {
                if (i < 0 || productName.charAt(i) == ' ') {
                    String substring = productName.substring(i + 1, startIndex);
                    if (!compile.matcher(substring).matches() && substring.length() < 4) {
                        startIndexResult.set(i + 1);
                        numberPrefix = substring;
                    }
                    break;
                }
            }

            if (endIndex < productName.length()) {
                while (productName.charAt(endIndex) == ' ') {
                    endIndex++;
                }
                for (int i = endIndex; i < productName.length(); i++) {
                    if (productName.charAt(i) == ' ' || i == productName.length() - 1) {
                        String substring = productName.substring(endIndex, i + 1);
                        if (!compile.matcher(substring).matches() && substring.length() < 4) {
                            endIndexResult.set(i + 1);
                            numberPostfix = substring;
                        }
                        break;
                    }
                }
            }
            List<String> result = new ArrayList<>();
            String bestGuessForModel =
                    (numberPrefix == null ? "" : numberPrefix) +
                            numberPart +
                            (numberPostfix == null ? "" : numberPostfix);
            result.add(bestGuessForModel);
            if (numberPrefix != null) {
                String withPrefix = numberPrefix + numberPart;
                if (!result.contains(withPrefix)) {
                    result.add(withPrefix);
                }
            }
            if (numberPostfix != null) {
                String withPostfix = numberPart + numberPostfix;
                if (!result.contains(withPostfix)) {
                    result.add(withPostfix);
                }
            }
            return result;
        }
        return null;
    }

    private String replaceDashUnderscore(String productName) {
        return productName.replaceAll("-", " ").replaceAll("_", " ").replaceAll("/", " ");
    }

    private void addField(Document doc, String fieldName, String fieldValue, float boost) {
        if (fieldValue != null) {
            Field field = boost == -1 ?
                    new StoredField(fieldName, fieldValue):
                    new Field(fieldName, replaceDashUnderscore(fieldValue), TextField.TYPE_STORED);
            if (boost != -1) {
                field.setBoost(boost);
            }
            doc.add(field);
        }
    }


    public Product findProduct(Listing listing, int i) throws IOException, ParseException {
        String title = listing.getTitle();
        String origManf = listing.getManufacturer();
        try {
            if (title.toLowerCase().contains("for")) {
                listing.setTitle(title.substring(0, title.toLowerCase().indexOf("for")));
            }
            if (listing.getManufacturer().endsWith(" Canada")) {
                listing.setManufacturer(listing.getManufacturer().substring(0, listing.getManufacturer().length() - " Canada".length()));
            }
            Query productQuery = new QueryParser(PRODUCT_NAME, new SynonymAnalyzer(stopWordsSet, synMap)).parse(getProductQueryString(listing.getTitle(), listing.getManufacturer(), -1f));
            long start = new Date().getTime();
            TopDocs productHits = productIndexSearcher.search(productQuery, 10);
            int prodResCount = productHits.scoreDocs.length;
            IndexSearcher listingsIndexSearcher = indexListing(listing);
            for (int prodIndex = 0; prodIndex < prodResCount; prodIndex++) {
//                if (productHits.scoreDocs[prodIndex].score <= 10) {
//                    break;
//                }
                int docID = productHits.scoreDocs[prodIndex].doc;
                Document productDoc = productIndexSearcher.doc(docID);
                Product result = new Product();
                result.setProductName(productDoc.get(PRODUCT_NAME + ORIGINAL));
                result.setFamily(productDoc.get(FAMILY + ORIGINAL));
                result.setManufacturer(productDoc.get(MANUFACTURER + ORIGINAL));
                result.setModel(productDoc.get(MODEL + ORIGINAL));
                result.setAnnouncedDate(productDoc.get(ANNOUNCED_DATE));
                Explanation explain = productIndexSearcher.explain(productQuery, docID);
                float score = productHits.scoreDocs[prodIndex].score;
                float reverseScore = 0;
                int listingDocId = 0;
//                    Document listingDoc =  listingsIndexSearcher.doc(listingDocId);
                StringBuffer sb = new StringBuffer("");

                String productName = result.getProductName();
                if (productName != null) {

                    AtomicInteger startIndex = new AtomicInteger(-1);
                    AtomicInteger endIndex = new AtomicInteger(-1);
                    List<String> numberParts = tryToGetModelNumber(productName, startIndex, endIndex);
                    String queryProductName = productName;
                    if (numberParts != null) {
                        if (result.getModel() != null) {
                            String modelReplaced = result.getModel().replaceAll("-", "").replaceAll("_", "").replaceAll(" ", "");
                            if (!numberParts.contains(modelReplaced)) {
                                numberParts.add(modelReplaced);
                            }
                        }
                        queryProductName = productName.substring(0, startIndex.get()) +
                                productName.substring(endIndex.get(), productName.length());
                        for (String numberPart : numberParts) {
                            getTermQuery(sb, PRODUCT_NAME, getQueryValue(numberPart), PRODUCT_NUMBER_BOOST);
                        }
                    }
                    getTermQuery(sb, PRODUCT_NAME, queryProductName, -1f);

                    String family = result.getFamily();
                    if (family != null) {
                        getTermQuery(sb, PRODUCT_NAME, getQueryValue(family), FAMILY_BOOST);
                    }
                    String model = result.getModel();
                    if (model != null) {
                        List<String> modelNumbers = tryToGetModelNumber(model, new AtomicInteger(-1), new AtomicInteger(-1));
                        if (modelNumbers != null) {
                            for (String modelNumber : modelNumbers) {
                                getTermQuery(sb, PRODUCT_NAME, getQueryValue(modelNumber), MODEL_BOOST);
                            }
                        } else {
                            getTermQuery(sb, PRODUCT_NAME, getQueryValue(model), MODEL_BOOST);
                        }
                    }
                }
                String manufacturer = result.getManufacturer();
                if (manufacturer != null && !getQueryValue(manufacturer).trim().equals("")) {
                    getTermQuery(sb, MANUFACTURER, getQueryValue(replaceCharsInManufacturer(manufacturer)), MANUFACTURER_BOOST * 5);
                }
                Explanation listingExpain = listingsIndexSearcher.explain(new QueryParser(PRODUCT_NAME, new SynonymAnalyzer(stopWordsSet, synMap)).parse(sb.toString().trim()), listingDocId);
                reverseScore = listingExpain.getValue();
//                    System.out.println("value = " + value);

                if (score > 31) {
                    Explanation[] details = explain.getDetails();
                    boolean manfMatched = false;
                    String matchedManufacturer = "";
                    for (Explanation detail : details) {
                        Matcher manufacturerMatcher = Pattern.compile(".*manufacturer\\:(\\w+).*").matcher(detail.getDescription());
                        if (manufacturerMatcher.matches()) {
                            manfMatched = true;
                            matchedManufacturer = manufacturerMatcher.group(1);
                        }
                    }
                    String indexedManufacturer = productDoc.get(MANUFACTURER);
                    if (!manfMatched) {
                        if (replaceDashUnderscore(listing.getManufacturer()).trim().equals("")) {
                            manfMatched = true;
                        }
                    }
                    if (!manfMatched) {
                        TopDocs manfSearchHits = productIndexSearcher.search(
                                new QueryParser(MANUFACTURER, new SynonymAnalyzer(stopWordsSet, synMap)).parse(getProductQueryString("", listing
                                        .getManufacturer(), -1f)), 1);
                        if (manfSearchHits.totalHits == 0) {
                            manfMatched = true;
                        }

                    }
                    if (manfMatched && reverseScore > 1.94) {
                        return result;
                    } else {
                        System.out.println("no match!");
                    }
//                } else if (productHits.scoreDocs[0].score > 19) {
//                    System.out.println("Almost match:" + result.getProductName() + " with " + listing.getTitle());
                }
            }
            return null;
        } finally {
            listing.setTitle(title);
            listing.setManufacturer(origManf);
        }
    }

    private String getProductQueryString(String title, String manufacturer, Float boost) {
        title = removeDuplicateTerms(title);
        StringBuffer sb = new StringBuffer("");

        if (title != null && !title.equals("")) {
            AtomicInteger startIndex = new AtomicInteger(-1);
            AtomicInteger endIndex = new AtomicInteger(-1);
            List<String> numberParts = tryToGetModelNumber(title, startIndex, endIndex);

            String queryProductName = title;
            if (numberParts != null) {
                queryProductName = title.substring(0, startIndex.get()) +
                        title.substring(endIndex.get(), title.length());
                for (String numberPart : numberParts) {
                    getTermQuery(sb, PRODUCT_NAME, getQueryValue(numberPart), PRODUCT_NUMBER_BOOST);
                    getTermQuery(sb, MODEL, getQueryValue(numberPart), boost);
                }
            }
            getTermQuery(sb, PRODUCT_NAME, getQueryValue(queryProductName), boost);
            getTermQuery(sb, FAMILY, getQueryValue(title), boost);
        }

        if (manufacturer != null && !getQueryValue(manufacturer).trim().equals("")) {
            getTermQuery(sb, MANUFACTURER, getQueryValue(replaceCharsInManufacturer(manufacturer)), MANUFACTURER_BOOST * 5);
        }
        return sb.toString();
    }

    private String replaceCharsInManufacturer(String manuf) {
        return replaceDashUnderscore(manuf);
    }

    private String removeDuplicateTerms(String title) {
        StringJoiner joiner = new StringJoiner(" ");
        List<String> terms = new ArrayList<>();
        String[] split = title.split(" ");
        for (String s : split) {
            boolean found = false;
            for (String term : terms) {
                if (term.equalsIgnoreCase(s)) {
                    found = true;
                }
            }
            if (!found) {
                terms.add(s);
                joiner.add(s);
            }
        }
        return joiner.toString();
    }

    private void getTermQuery(StringBuffer sb, String attributeName, String queryValue, Float boost) {
        String[] split = queryValue.split(" ");
        for (String s : split) {
            if (s.trim().length() < 2 || s.trim().equalsIgnoreCase("and") || s.trim().equalsIgnoreCase("or")) {
                continue;
            }
            sb.append(attributeName);
            sb.append(":");
            sb.append(s);
            if (!boost.equals(-1f)) {
                sb.append("^");
                sb.append(boost);
            }
            sb.append(" ");
        }
    }

    private String getQueryValue(String value) {
        return QueryParser.escape(replaceDashUnderscore(value));
    }

    /**
     * finds the Product matching the listing
     * @param listing
     * @return
     */
//    public Product findProduct(Listing listing) {
//        IProductMatcher matcher = new WeightedMatcher();
//        Map<Product, Double> matchedProducts = new HashMap<Product, Double>();
//        for (Product product : products) {
//            MatchResult matchResult = matcher.match(product, listing);
//            if (matchResult.getResemblanceScore() > THRESHOLD) {
//                matchedProducts.put(product, matchResult.getResemblanceScore());
//            }
//        }
//        if (matchedProducts.size() == 1) {  // if only one product productIndexSearcher found, return it
//            return matchedProducts.keySet().iterator().next();
//        }
//        if (matchedProducts.size() > 1) {   // if more than one product productIndexSearcher found,
//                                            // return the product with max score if there productIndexSearcher no other product with a close score
//            Collection<Double> resemblanceScores = new ArrayList<Double>(matchedProducts.values());
//            Double max = Collections.max(resemblanceScores); // get the max score
//            resemblanceScores.remove(max); // remove from the scores to find the minimum difference
//            Double minDiff = Double.MAX_VALUE;
//            for (Double resemblanceScore : resemblanceScores) {
//                double diff = max - resemblanceScore;
//                if (diff < minDiff) {
//                    minDiff = diff;
//                }
//            }
//            if (minDiff > 0.2) {// if minimum difference productIndexSearcher more than 0.2, we can return the max score
//                for (Product product : matchedProducts.keySet()) {
//                    if (matchedProducts.get(product) == max) {
//                        return product;
//                    }
//                }
//            }
////            System.out.println("{\"listing\":" + listing + ", \"matched\":" + matchedProducts.keySet());
//            return null; // when there are two products with very close scores, return none of them!
//        }
//        return null;
//    }
}
