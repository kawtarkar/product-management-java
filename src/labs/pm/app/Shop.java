package labs.pm.app;

import labs.pm.data.Product;
import labs.pm.data.ProductManager;
import labs.pm.data.ProductManagerException;
import labs.pm.data.Rating;

import java.math.BigDecimal;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;

public class Shop {
    public static void main(String[] args) {

        ProductManager pm = new ProductManager("en-GB") ;
//        pm.creatProduct( 101, "Tea", BigDecimal.valueOf (1.99), Rating.NOT_RATED, null) ;
//        pm.printProductReport(101);
        pm.parseProduct("D,101,Tea,1.99,0,2021-10-4");
        pm.parseReview("101,4,Nice cup of tea ");
        pm.parseReview("101,2,Bad tea ");
        pm.parseReview("101,3,Not too bad ");
        pm.parseReview("101,4,Fantastic tea  ");
        pm.printProductReport(101);





//
//        Product p1 = pm.creatProduct( 101, "Tea", BigDecimal.valueOf (1.99), Rating.NOT_RATED, null) ;
//        p1 = pm.reviewProduct (101, Rating.FOUR_STAR, "Nice hot cup of tea") ;
//        Product p2 = pm.creatProduct( 102, "food", BigDecimal.valueOf (20), Rating.NOT_RATED, LocalDate.of(2027,10,10)) ;
//        Product p3 = pm.creatProduct( 103, "fish", BigDecimal.valueOf (70), Rating.NOT_RATED, LocalDate.of(2027,10,20)) ;
//        p3 = pm.reviewProduct (103, Rating.THREE_STAR, "Just add some lemon") ;
//        Product p4 = pm.creatProduct( 104, "potato", BigDecimal.valueOf (20), Rating.NOT_RATED, LocalDate.of(2027,10,10)) ;
//        pm.printProductReport (19);
//        pm.reviewProduct (19, Rating.THREE_STAR, "Just add some lemon") ;
//
//        p1 = pm.reviewProduct (101, Rating.TWO_STAR, "Rather weak tea") ;
//        p1 = pm.reviewProduct (101, Rating.FOUR_STAR, "Fine tea") ;
//        p1 = pm.reviewProduct (101, Rating.FOUR_STAR, "Good tea");
//        p1 = pm.reviewProduct (101, Rating.FIVE_STAR, "Perfect tea");
//        pm.printProductReport (101);
//        Comparator<Product> ratingSorter = (px, py)->py.getRating().ordinal()-px.getRating().ordinal();
//        Comparator<Product> priceSorter =(px,py)->py.getPrice().compareTo(px.getPrice());
//        pm.printProducts(ratingSorter.thenComparing(priceSorter));
//        pm.printProducts(ratingSorter.thenComparing(priceSorter).reversed());
//        pm.printProducts( (px, py)->py.getRating().ordinal()-px.getRating().ordinal(),p->p.getPrice().floatValue()>2);
        }
    }
