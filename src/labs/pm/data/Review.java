/*
 * Copyright (c) 2025. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package labs.pm.data;

import java.io.Serializable;

/**
 * @author kawtar
 **/
public record Review (Rating rating, String comments) implements Comparable<Review> , Serializable {
//    private Rating rating;
//    private String comments;
//
//    public Review(Rating rating, String comments) {
//        this.rating = rating;
//        this.comments = comments;
//    }
//
//    @Override
//    public String toString() {
//        return "Rating {" + "rating=" + rating + ", comments=" + comments + "}";
//    }
//
//    public Rating getRating() {
//        return rating;
//    }
//
//    public String getComments() {
//        return comments;
//    }

    @Override
    public int compareTo(Review other) {
        return other.rating().ordinal()-this.rating().ordinal();
    }
}