/*
 * Copyright (c) 2025. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package labs.pm.data;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author kawtar
 **/
public class ProductManager {
    private Product product;
    private Review review;
    private ResourceFormatter formatter;
    private static Map<String, ResourceFormatter> formatters =
            Map.of ("en-GB", new ResourceFormatter(Locale.UK) ,
                    "en-US", new ResourceFormatter(Locale.US) ,
                    "ru-RU", new ResourceFormatter (new Locale ("ru", "RU")),
                    "fr-FR", new ResourceFormatter (Locale.FRANCE) ,
                    "zh-CN", new ResourceFormatter (Locale.CHINA) ) ;

    private static final Logger logger = Logger.getLogger(ProductManager.class.getName());
    private Map<Product, List<Review>> products= new HashMap<>();
    private ResourceBundle config = ResourceBundle.getBundle("labs.pm.data.config");
    private MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));
    private MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));

    private Path reportsFolder = Path.of(config.getString("reports.folder"));
    private Path dataFolder = Path.of(config.getString("data.folder"));
    private Path tempFolder = Path.of(config.getString("temp.folder"));

    public ProductManager (String languageTag) {
        changeLocale (languageTag) ;
    }
    public ProductManager (Locale locale) {
        this(locale.getLanguage());
    }
    public void changeLocale (String languageTag) {
        formatter = formatters.getOrDefault (languageTag, formatters.
                get ("en-GB") ) ;
    }
    public static Set<String> getSupportedLocales () {
        return formatters.keySet () ;
    }

    public Product creatProduct(int id , String name,
                                BigDecimal price, Rating rating, LocalDate bestBefore) {
       product=new Food (id,name,price,rating,bestBefore);
       products.putIfAbsent(product,new ArrayList<>());
        return product;
    }
    public Product creatProduct(int id , String name,
                                BigDecimal price, Rating rating) {
        product= new Drink (id,name,price,rating);
        products.putIfAbsent(product,new ArrayList<>());
        return  product;
    }
    public Product findProduct(int id) throws ProductManagerException{

            return products.keySet()
                    .stream()
                    .filter(p -> p.getId() == id)
                    .findFirst().orElseThrow(
                            ()->new ProductManagerException("Product with id "+ id +" not found"));

    }

    public Product reviewProduct (Product product, Rating rating, String comments){
        List<Review> reviews=products.get(product);
        products.remove(product);
        reviews.add(new Review(rating, comments));
        product=product.applyRating(
                Rateable.convert(
                (int)Math.round(
                reviews.stream().mapToInt(r->r.getRating().ordinal()).average().orElse(0))));
        products.put(product,reviews);
        return product;

    }
    public Product reviewProduct (int productId, Rating rating, String comments){
        try {
            return reviewProduct(findProduct(productId),rating,comments);
        } catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
            return null;
        }

    }

    public void printProductReport (int productId) {
        try {
            printProductReport(findProduct(productId));
        } catch (ProductManagerException e) {
            logger.log(Level.INFO,e.getMessage());

        } catch (IOException e) {
            logger.log(Level.SEVERE,"Error printing product report "+e.getMessage(),e);
        }
    }
    public void printProductReport (Product product) throws IOException {
        List<Review> reviews = products.get(product);
        Collections.sort(reviews);
        Path productFile = reportsFolder.resolve(MessageFormat.format(config.getString("report.file"),product.getId()));

        try(PrintWriter out = new PrintWriter(
                new OutputStreamWriter(
                        Files.newOutputStream(productFile, StandardOpenOption.CREATE),"UTF-8"))) {
            out.append(formatter.formatProduct(product)+System.lineSeparator());
            if(reviews.isEmpty()){
                out.append(formatter.getText("no.reviews")+System.lineSeparator());
            }
            else{
                out.append(reviews.stream()
                    .map(r->formatter.formatReview(r)+System.lineSeparator())
                    .collect(Collectors.joining()));
            }
        }

//        StringBuilder txt = new StringBuilder ();
//        txt.append(formatter.formatProduct(product));
//        txt.append('\n');
//        if(reviews.isEmpty()){
//            txt.append(formatter.getText("no.reviews")+'\n');
//        }
//        else{
//            txt.append(reviews.stream()
//                    .map(r->formatter.formatReview(r)+'\n')
//                    .collect(Collectors.joining()));
//        }
//
//            System.out.println(txt);
    }
    public void printProducts(Comparator<Product> sorted, Predicate<Product> filter){

        StringBuilder txt = new StringBuilder();

        products.keySet()
                .stream()
                .sorted(sorted)
                .filter(filter)
                .forEach(p->txt.append(formatter.formatProduct(p)+'\n'));

        System.out.println(txt);
    }
    public void parseReview  (String text) {
        try {
            Object [] values =reviewFormat.parse(text);
            reviewProduct(Integer.parseInt((String)values[0]),
                    Rateable.convert(Integer.parseInt((String)values[1])), (String)values[2]);
        } catch (ParseException | NumberFormatException e) {
            logger.log(Level.WARNING,"Error parsing review"+text,e);

        }
    }
    public void parseProduct(String text){
        try {
            Object [] values =productFormat.parse(text);
            int id = Integer.parseInt((String)values[1]);
            String name = (String)values[2];
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble((String)values[3]));
            Rating rating = Rateable.convert(Integer.parseInt((String)values[4]));
            switch ((String)values[0]){
                case "D":
                    creatProduct(id,name,price,rating);
                    break;
                case "F":
                    LocalDate bestBefore = LocalDate.parse((String)values[5]);
                    creatProduct(id,name,price,rating,bestBefore);
                    break;
            }

        } catch (ParseException | NumberFormatException | DateTimeParseException e) {
            logger.log(Level.WARNING,"Error parsing product "
                    +"text"+ e.getMessage());
        }

    }
//    public Map <String,String> getDiscount(){
//        return products.keySet()
//                .stream()
//                .collect(
//                        Collectors.groupingBy(
//                                product->product.getRating().getStars()),
//                        Collectors.summarizingDouble(product->product.getDiscount().doubleValue),
//                        discount->formatter.moneyFormat.format(discount));
//    }

    private static class ResourceFormatter{
        private Locale locale;
        private ResourceBundle resources;
        private DateTimeFormatter dateFormat;
        private NumberFormat moneyFormat;

        private ResourceFormatter(Locale local){
            this.locale=local;
            resources = ResourceBundle.getBundle("labs.pm.data.resources", locale);
            dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
            moneyFormat = NumberFormat.getCurrencyInstance(locale);
        }
        private String formatProduct(Product product){
            String type = (product instanceof Food) ?
                    resources.getString ("food") : resources.getString ("drink");
            return MessageFormat.format(resources.getString("product"), product.getName(),
                    moneyFormat.format(product.getPrice()),
                    product.getRating().getStars(), type
//                dateFormat.format(product.
                    );
        }
        private String formatReview(Review review){
            return MessageFormat.format (resources.getString ("review"),
                    review.getRating().getStars(),
                    review.getComments() );
        }
        private String getText (String key) {
            return resources.getString (key) ;
        }

    }


}
