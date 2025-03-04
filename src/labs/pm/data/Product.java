/*
 * Copyright (c) 2025. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package labs.pm.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * @author kawtar
 **/
public sealed abstract class Product
        implements Rateable<Product>, Serializable permits Food,Drink{
    private int id;
    private String name;
    private BigDecimal price;
    public static final BigDecimal DISCOUNT_RATE =  BigDecimal.valueOf(0.1);
    private Rating rating ;

    Product(int id, String name, BigDecimal price, Rating rating) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.rating = rating;
    }

    @Override
    public  boolean equals(Object o ){
        if (this==o) return true;
        if (o instanceof Product product){
            return this.id==product.id;
        }
        return false;
    }


    public abstract Product applyRating(Rating newRating) ;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }


    public Rating getRating() {
        return rating;
    }
}
