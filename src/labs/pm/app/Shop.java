package labs.pm.app;

import labs.pm.data.Product;
import labs.pm.data.ProductManager;
import labs.pm.data.Rating;

import java.math.BigDecimal;

import java.util.Locale;

public class Shop {
    public static void main(String[] args) {

        ProductManager pm = new ProductManager("ru-RU") ;
        Product p1 = pm.creatProduct( 101, "Tea", BigDecimal.valueOf (1.99), Rating.NOT_RATED, null) ;
        pm.printProductReport (101) ;
        p1 = pm.reviewProduct (101, Rating.FOUR_STAR, "Nice hot cup of tea") ;
        p1 = pm.reviewProduct (101, Rating.TWO_STAR, "Rather weak tea") ;
        p1 = pm.reviewProduct (101, Rating.FOUR_STAR, "Fine tea") ;
        p1 = pm.reviewProduct (101, Rating.FOUR_STAR, "Good tea");
        p1 = pm.reviewProduct (101, Rating.FIVE_STAR, "Perfect tea");
        p1 = pm.reviewProduct (101, Rating.THREE_STAR, "Just add some lemon") ;
        pm.printProductReport (101);
        }
    }
