package labs.pm.app;

import labs.pm.data.Product;
import labs.pm.data.ProductManager;
import labs.pm.data.ProductManagerException;
import labs.pm.data.Rating;

import java.math.BigDecimal;

import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Shop {
    public static void main(String[] args) {
        ProductManager pm = ProductManager.getInstance();
        AtomicInteger clientCount = new AtomicInteger(0);
        Callable<String> client = ()->{
            String clientId = "client "+ clientCount.incrementAndGet();
            String threadName =Thread.currentThread().getName();
            int productId = ThreadLocalRandom.current().nextInt(63)+101;
            String languageTag = ProductManager.getSupportedLocales()
                    .stream()
                    .skip(ThreadLocalRandom.current().nextInt(4))
                    .findFirst().get();
            StringBuilder log = new StringBuilder();
            log.append(clientId+"  "+threadName+"\n-\tstart of log\t-\n");
            Product product = pm.reviewProduct (productId, Rating.FOUR_STAR, "Fine tea") ;
            log.append((product==null)
                    ?"\nProduct "+productId+"reviewed\n"
                    :"\nProduct "+productId+"not reviewed\n");
            pm.printProductReport(productId,languageTag,clientId);
            log.append(clientId+" generate report for "+productId+"product");


            log.append("\n-\tend of log\t-\n");
            return "";
        };
        List<Callable<String>> clients = Stream.generate(()->client)
                .limit(5)
                .collect(Collectors.toList());


        ExecutorService executorService = Executors.newFixedThreadPool(3);
        try {
            List<Future<String>> results = executorService.invokeAll(clients);
            executorService.shutdown();
            results.stream().forEach(result->{
                try {
                    System.out.println(result.get());
                } catch (InterruptedException |ExecutionException e) {
                    Logger.getLogger(Shop.class.getName()).log(Level.SEVERE,"Error retrieving client log",e);
                }
            });
        } catch (InterruptedException e) {
            Logger.getLogger(Shop.class.getName()).log(Level.SEVERE,"Error invoking clients",e);
        }


//        ProductManager pm = new ProductManager("en-GB") ;
//        pm.printProductReport(101);
//        pm.creatProduct( 102, "honey", BigDecimal.valueOf (1.99), Rating.NOT_RATED, null) ;
//        pm.reviewProduct (102, Rating.TWO_STAR, "Rather weak tea") ;
//        pm.reviewProduct (102, Rating.FOUR_STAR, "Fine tea") ;
//        pm.reviewProduct (102, Rating.FOUR_STAR, "Good tea");
//        pm.reviewProduct (102, Rating.FIVE_STAR, "Perfect tea");

//        pm.parseProduct("D,101,Tea,1.99,0,2021-10-4");
//        pm.parseReview("101,4,Nice cup of tea ");
//        pm.parseReview("101,2,Bad tea ");
//        pm.parseReview("101,3,Not too bad ");
//        pm.parseReview("101,4,Fantastic tea  ");

//        pm.printProductReport(101);
//        pm.printProducts( (px, py)->
//                py.getRating().ordinal()-px.getRating().ordinal(),p->p.getPrice().floatValue()<2);
//
//







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
