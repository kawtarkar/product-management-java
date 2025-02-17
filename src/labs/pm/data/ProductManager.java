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

/**
 * @author kawtar
 **/
public class ProductManager {
    private Product product;
    private Review review;
    private Locale locale;
    private ResourceBundle resources;
    private DateTimeFormatter dateFormat;
    private NumberFormat moneyFormat;

    private Map<Product, List<Review>> products= new HashMap<>();


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

    public Product reviewProduct (Product product, Rating rating, String comments){
        List<Review> reviews=products.get(product);
        products.remove(product);
        reviews.add(new Review(rating, comments));
        int sum =0 ;
        for(Review review: reviews){
            sum+=review.getRating().ordinal();
        }
        product=product.applyRating(Rateable.convert(Math.round((float)sum/reviews.size())));
        products.put(product,reviews);
        return product;

    }
    public ProductManager (Locale locale) {

        this.locale = locale;
        resources = ResourceBundle.getBundle("labs.pm.data.resources", locale);
        dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
        moneyFormat = NumberFormat.getCurrencyInstance(locale);
    }
    public void printProductReport (Product product) {
        List<Review> reviews = products.get(product);
        StringBuilder txt = new StringBuilder ();
        String type = (product instanceof Food) ?
                    resources.getString ("food") : resources.getString ("drink");
        txt.append(MessageFormat.format(resources.getString("product"), product.getName(),
                moneyFormat.format(product.getPrice()),
                product.getRating().getStars(), type
//                dateFormat.format(product.
        ));
        txt.append('\n');
        for(Review review : reviews){
            txt.append (MessageFormat.format (resources.getString ("review"),
                    review.getRating().getStars(),
                    review.getComments() )) ;
            txt.append('\n');
        }
        if(reviews.isEmpty()){
            txt.append(resources.getString("no.reviews"));
            txt.append('\n');

        }
            System.out.println(txt);
    }

}
