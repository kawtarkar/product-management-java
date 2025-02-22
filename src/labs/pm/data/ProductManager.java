/*
 * Copyright (c) 2025. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package labs.pm.data;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Predicate;
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


    private Map<Product, List<Review>> products= new HashMap<>();

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
    public Product findProduct (int id){
        return products.keySet()
                .stream()
                .filter(p->p.getId()==id)
                .findFirst().orElseGet(null);

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
        return reviewProduct(findProduct(productId),rating,comments);

    }

    public void printProductReport (int productId){
        printProductReport(findProduct(productId));
    }
    public void printProductReport (Product product) {
        List<Review> reviews = products.get(product);
        Collections.sort(reviews);
        StringBuilder txt = new StringBuilder ();
        txt.append(formatter.formatProduct(product));
        txt.append('\n');

        if(reviews.isEmpty()){
            txt.append(formatter.getText("no.reviews")+'\n');
        }
        else{
            txt.append(reviews.stream()
                    .map(r->formatter.formatReview(r)+'\n')
                    .collect(Collectors.joining()));
        }

            System.out.println(txt);
    }
    public void printProducts(Comparator<Product> sorted, Predicate<Product> filter){
//        List<Product> productList = new ArrayList<>(products.keySet());
//       productList.sort(sorted);

        StringBuilder txt = new StringBuilder();

        products.keySet()
                .stream()
                .sorted(sorted)
                .filter(filter)
                .forEach(p->txt.append(formatter.formatProduct(p)+'\n'));
//        for (Product product : productList) {
//            txt.append(formatter.formatProduct(product));
//            txt.append('\n');
//
//        }
        System.out.println(txt);
    }


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
